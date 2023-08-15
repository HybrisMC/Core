/*
 * HybrisMC, a Minecraft toolchain and client
 * Copyright (C) 2023, The HybrisMC Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.hybrismc.core

import com.grappenmaker.jvmutil.*
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.TraceClassVisitor
import java.io.PrintWriter
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.full.findAnnotation

@Retention
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class Named(vararg val names: String)

// Allow using an annotation to declare to not implement a method when the implementation is missing
@Retention
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class OmitMissingImplementation

@Retention
@Target(AnnotationTarget.CLASS)
annotation class OmitAllMissing

// Annotation that specfies a method is a ctor
@Retention
@Target(AnnotationTarget.FUNCTION)
annotation class ConstructorAccess

interface StaticAccessor<T : Any>

// This exists so in no case duplicate names arise
private var accessorCounter = 0
    get() = field++

data class DirectAccessorData(
    val targets: Set<String>,
    val virtualType: Class<*>,
    val staticType: Class<*>,
    val loader: ClassLoader,
    val allowNotImplemented: Boolean = true,
) {
    init {
        require(virtualType.isInterface && staticType.isInterface) { "Types were not interfaces!" }
    }

    var staticImpl: Any? = null

    inline fun <reified T : Any> static(): T {
        ensureLoaded()
        if (staticImpl == null) error("Static implementation has not been generated (yet?)")
        return staticImpl as? T ?: error("Static implementation does not match type?")
    }

    private val isLoaded get() = staticImpl != null

    fun ensureLoaded() {
        if (!isLoaded) targets.firstNotNullOfOrNull { runCatching { loader.loadClass(it) }.getOrNull() }
        assert(staticImpl != null)
    }

    private fun MethodVisitor.returnCasting(returnType: Type) {
        if (!returnType.isPrimitive) cast(returnType.internalName)
        returnMethod(returnType.getOpcode(IRETURN))
    }

    private fun MethodVisitor.loadParameters(from: Array<Type>, to: Array<Type>) {
        var paramIndex = 1
        for ((idx, paramType) in from.withIndex()) {
            load(paramIndex, paramType)

            // Find the correct target type of this parameter
            val targetType = to[idx].internalName

            // Cast when not matching type (could fail, Prayge)
            if (!paramType.isPrimitive && paramType.internalName != targetType) cast(targetType)

            // Increment param index, keeping doubles and longs in mind
            paramIndex += paramType.size
        }
    }

    private fun MethodVisitor.implementField(impl: FieldImplementation, isStatic: Boolean) {
        val isSetter = impl.type == FieldImplementation.Type.SETTER
        val desc = Type.getMethodDescriptor(impl.receiverMethod)
        val desiredType = Type.getReturnType(desc)

        if (!isStatic) loadThis()
        if (isSetter) {
            val setterType = Type.getArgumentTypes(desc).first()
            val fieldType = Type.getType(impl.target.field.desc)
            loadParameters(arrayOf(setterType), arrayOf(fieldType))
        }

        if (isSetter) setField(impl.target) else getField(impl.target)
        if (isSetter) returnMethod() else returnCasting(desiredType)
    }

    private fun MethodVisitor.implementMethod(impl: MethodImplementation, isStatic: Boolean) {
        val desc = Type.getMethodType(Type.getMethodDescriptor(impl.receiverMethod))
        if (!isStatic) loadThis()

        if (impl.receiverMethod.isAnnotationPresent(ConstructorAccess::class.java)) {
            visitTypeInsn(NEW, impl.target.owner.name)
            dup()
        }

        loadParameters(desc.argumentTypes, impl.target.method.arguments)
        invokeMethod(impl.target)
        returnCasting(desc.returnType)
    }

    private fun MethodVisitor.implementMissing(impl: NotImplementedImplementation) {
        if (impl.throwException) {
            construct("java/lang/UnsupportedOperationException", "(Ljava/lang/String;)V") {
                loadConstant("Method is missing, was not implemented")
            }

            visitInsn(ATHROW)
        } else {
            val returnType = Type.getReturnType(impl.receiverMethod)
            visitInsn(returnType.stubLoadInsn)
            visitInsn(returnType.getOpcode(IRETURN))
        }
    }

    private fun Class<*>.selectImplementations(node: ClassNode, isStatic: Boolean) =
        declaredMethods.filterNot { it.isDefault || Modifier.isStatic(it.modifiers) }.map { receiverMethod ->
            val fields = node.fieldData.filter { it.field.isStatic == isStatic }.associateBy { it.field.name }
            val names = buildSet {
                add(receiverMethod.name)
                if (receiverMethod.isAnnotationPresent(ConstructorAccess::class.java) && isStatic) add("<init>")
                addAll(receiverMethod.getAnnotation(Named::class.java)?.names?.toSet() ?: emptySet())
            }

            val receiverArguments = Type.getArgumentTypes(receiverMethod)
            val paramCount = receiverMethod.parameterCount
            val isGetter = FieldImplementation.Type.GETTER.match(paramCount, receiverMethod.name)
            val isSetter = FieldImplementation.Type.SETTER.match(paramCount, receiverMethod.name)

            val potentialField = if (isGetter || isSetter) {
                names.firstNotNullOfOrNull { f ->
                    val stripped = f.drop(3)
                    fields[stripped] ?: fields[stripped.replaceFirstChar { it.lowercaseChar() }]
                }
            } else fields.values.find { it.field.name in names }

            if (potentialField != null && potentialField.field.isFinal && isSetter)
                error("Setter was declared for field $potentialField, but is final")

            val potentialMethod = node.methodData.find { (_, method) ->
                val arguments = Type.getArgumentTypes(method.desc)
                method.name in names && arguments.size == receiverArguments.size && receiverArguments.zip(arguments)
                    .filter { (t) -> t.isPrimitive || isSystemClass(t.className) }.all { (a, b) -> a == b }
                        && (!node.isAbstract || method.name != "<init>")
                        && (method.isStatic == isStatic || method.name == "<init>")
            }

            potentialField?.let { FieldImplementation(receiverMethod, it) }
                ?: potentialMethod?.let { MethodImplementation(receiverMethod, it) }
                ?: if (allowNotImplemented) NotImplementedImplementation(
                    receiverMethod,
                    throwException = !(receiverMethod.isAnnotationPresent(OmitMissingImplementation::class.java) ||
                            virtualType.isAnnotationPresent(OmitAllMissing::class.java))
                ) else error(
                    "No implementation was found for $receiverMethod (${Type.getMethodDescriptor(receiverMethod)})"
                )
        }

    private fun ClassVisitor.generateImpl(
        impl: DirectAccessorImplementation,
        basedOn: ClassNode,
        static: Boolean = false
    ) {
        val (name, desc) = impl.receiverMethod.asDescription()
//        if (!static && basedOn.methods.any { it.name == name && it.desc == desc }) return

        generateMethod(name, desc, access = ACC_PUBLIC or if (!static && basedOn.isInterface) 0 else ACC_FINAL) {
            when (impl) {
                is FieldImplementation -> implementField(impl, static)
                is MethodImplementation -> implementMethod(impl, static)
                is NotImplementedImplementation -> implementMissing(impl)
            }
        }
    }

    // This injects in the given node too
    private fun implementVirtual(node: ClassNode) {
        // Only for virtual, static has *freedom*
        val toImplement = virtualType.selectImplementations(node, false).filterNot { impl ->
            val desc = Type.getMethodDescriptor(impl.receiverMethod)
            node.methods.any { it.name == impl.receiverMethod.name && it.desc == desc }
        }

        // Can safely assume the interface will be there
        node.interfaces.add(virtualType.internalName)
        toImplement.forEach { node.generateImpl(it, node) }

        if (node.isInterface) {
            val currentVersion = node.version and 0xFFFF
            val newVersion = currentVersion.coerceAtLeast(52)
            node.version = node.version and 0xFFFF.inv() or newVersion
        }
    }

    // This creates a new class, and only uses the data in the node
    private fun implementStatic(node: ClassNode, dump: Boolean) = generateDefaultClass(
        name = "${staticType.simpleName}$accessorCounter",
        implements = listOf(staticType.internalName),
        debug = dump
    ) {
        staticType.selectImplementations(node, true).forEach { generateImpl(it, node, true) }
    }.instance<Any>()

    fun transform(node: ClassNode, dump: Boolean) {
        implementVirtual(node)
        staticImpl = implementStatic(node, dump)
    }
}

object DirectAccessors {
    private var dump = false
    val accessorData = mutableListOf<DirectAccessorData>()

    fun accessorByTarget(name: String) = accessorData.find { name in it.targets }
    fun accessorByType(name: String) = accessorData.find { it.virtualType.internalName == name }
    fun accessorByType(type: Class<*>) = accessorData.find { it.virtualType == type }
    fun accessorByType(type: Type) = accessorData.find { it.virtualType.internalName == type.internalName }
    inline fun <reified T : Any> accessorByType() = accessorByType(T::class.java)

    fun enableDump() {
        dump = true
    }

    fun transform(name: String, bytes: ByteArray): ByteArray? {
        val accessor = accessorByTarget(name) ?: return null

        // Not use asClassNode because then we cannot use ClassReader
        val reader = ClassReader(bytes)
        val writer = ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
        val node = ClassNode().also { reader.accept(it, 0) }

        return runCatching {
            accessor.transform(node, dump)
            node.accept(if (dump) TraceClassVisitor(writer, PrintWriter(System.out)) else writer)
            writer.toByteArray()
        }.onFailure {
            println("Failed to generate accessor for $name:")
            it.printStackTrace()
        }.getOrNull()
    }
}

sealed interface DirectAccessorImplementation {
    val receiverMethod: Method
}

data class FieldImplementation(
    override val receiverMethod: Method,
    val target: FieldData,
    val type: Type = if (receiverMethod.name.startsWith("set")) Type.SETTER else Type.GETTER
) : DirectAccessorImplementation {
    enum class Type(val paramCount: Int, val namePrefix: String) {
        GETTER(0, "get"), SETTER(1, "set");

        fun match(params: Int, name: String) = paramCount == params && name.startsWith(namePrefix) && name.length > 3
    }
}

data class MethodImplementation(
    override val receiverMethod: Method,
    val target: MethodData
) : DirectAccessorImplementation

data class NotImplementedImplementation(
    override val receiverMethod: Method,
    val throwException: Boolean
) : DirectAccessorImplementation

inline fun <reified V : Any, reified S : StaticAccessor<V>> accessor(
    allowNotImplemented: Boolean = true,
    loader: ClassLoader = V::class.java.classLoader
) = DirectAccessorData(
    V::class.findAnnotation<Named>()?.names?.asList()?.toSet() ?: error("No names were specified for ${V::class}"),
    virtualType = V::class.java,
    staticType = S::class.java,
    loader, allowNotImplemented
).also { DirectAccessors.accessorData += it }