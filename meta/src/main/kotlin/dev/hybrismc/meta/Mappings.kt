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

package dev.hybrismc.meta

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.MethodRemapper
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.commons.SimpleRemapper
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

// Associated per namespace
sealed interface Mapped {
    val names: List<String>
}

sealed interface Commented {
    val comments: List<String>
}

data class MappedClass(
    override val names: List<String>,
    override val comments: List<String>,
    val fields: List<MappedField>,
    val methods: List<MappedMethod>
) : Mapped, Commented

data class MappedMethod(
    override val names: List<String>,
    override val comments: List<String>,
    val desc: String,
    val parameters: List<MappedParameter>,
    val variables: List<MappedLocal>,
) : Mapped, Commented

data class MappedLocal(
    val index: Int,
    val startOffset: Int,
    val lvtIndex: Int,
    override val names: List<String>
) : Mapped

data class MappedParameter(
    override val names: List<String>,
    val index: Int,
) : Mapped

data class MappedField(
    override val names: List<String>,
    override val comments: List<String>,
    val desc: String?,
) : Mapped, Commented

sealed interface Mappings {
    val namespaces: List<String>
    val classes: List<MappedClass>
}

fun Mappings.namespace(name: String) = namespaces.indexOf(name).also { if (it == -1) error("Invalid namespace $name") }
fun Mappings.asSimpleRemapper(from: String, to: String) = SimpleRemapper(asASMMapping(from, to))

fun MappedField.index(owner: String, namespace: Int) = "$owner.${names[namespace]}"
fun MappedMethod.index(owner: String, namespace: Int) = "$owner.${names[namespace]}$desc"

fun Mappings.asASMMapping(from: String, to: String) = buildMap {
    val fromIndex = namespaces.indexOf(from)
    val toIndex = namespaces.indexOf(to)

    require(fromIndex >= 0) { "Namespace $from does not exist!" }
    require(toIndex >= 0) { "Namespace $to does not exist!" }
    classes.forEach { clz ->
        val owner = clz.names[fromIndex]
        put(owner, clz.names[toIndex])
        clz.fields.forEach { put(it.index(owner, fromIndex), it.names[toIndex]) }
        clz.methods.forEach { put(it.index(owner, fromIndex), it.names[toIndex]) }
    }
}

data class TinyMappings(
    override val namespaces: List<String>,
    override val classes: List<MappedClass>,
    val isV2: Boolean
) : Mappings

sealed interface MappingsFormat<T : Mappings> {
    fun detect(lines: List<String>): Boolean
    fun parse(lines: List<String>): T
}

val allMappingsFormats = listOf(TinyMappingsV1Format, TinyMappingsV2Format, SRGMappingsFormat, XSRGMappingsFormat)
fun loadMappings(lines: List<String>) = allMappingsFormats.find { it.detect(lines) }?.parse(lines)
    ?: error("No format was found for mappings")

object TinyMappingsV1Format : MappingsFormat<TinyMappings> by TinyMappingsFormat(false)
object TinyMappingsV2Format : MappingsFormat<TinyMappings> by TinyMappingsFormat(true)

class TinyMappingsFormat(private val isV2: Boolean) : MappingsFormat<TinyMappings> {
    // If v1: check if line starts with v1 and all other lines are prefixed correctly
    // If v2: check if line starts with tiny
    override fun detect(lines: List<String>) =
        lines.firstOrNull()?.parts()?.first() == (if (isV2) "tiny" else "v1") && (isV2 || lines.drop(1).all {
            it.startsWith("CLASS") || it.startsWith("FIELD") || it.startsWith("METHOD")
        })

    // Quirk: empty name means take the last name
    private fun List<String>.fixNames() = buildList {
        val (first, rest) = this@fixNames.splitFirst()
        require(first.isNotEmpty()) { "first namespaced name is not allowed to be empty in tiny!" }
        add(first)

        rest.forEach { add(it.ifEmpty { last() }) }
    }

    override fun parse(lines: List<String>): TinyMappings {
        require(detect(lines))

        // FIXME: Skip meta for now
        val info = (lines.firstOrNull() ?: error("Mappings are empty!")).parts()
        val namespaces = info.drop(if (isV2) 3 else 1)
        val mapLines = lines.drop(1).dropWhile { it.countStart() != 0 }.filter { it.isNotBlank() }

        return TinyMappings(namespaces, if (isV2) {
            var state: TinyV2State = MappingState()
            mapLines.forEach { state = state.update(it) }
            state = state.end()

            (state as? MappingState ?: error("Did not finish walking tree, parsing failed (ended in $state)")).classes
        } else {
            val parted = lines.map { it.parts() }
            val methods = parted.collect("METHOD") { entry ->
                val (desc, names) = entry.splitFirst()
                MappedMethod(
                    names = names.fixNames(),
                    comments = listOf(),
                    desc = desc,
                    parameters = listOf(),
                    variables = listOf()
                )
            }

            val fields = parted.collect("FIELD") { entry ->
                val (desc, names) = entry.splitFirst()
                MappedField(
                    names = names.fixNames(),
                    comments = listOf(),
                    desc = desc
                )
            }

            parted.filter { (type) -> type == "CLASS" }.map { entry ->
                MappedClass(
                    names = entry.drop(1).fixNames(),
                    comments = listOf(),
                    fields = fields[entry[1]] ?: listOf(),
                    methods = methods[entry[1]] ?: listOf(),
                )
            }
        }, isV2)
    }

    private inline fun <T> List<List<String>>.collect(type: String, mapper: (List<String>) -> T) =
        filter { (t) -> t == type }
            .groupBy { (_, unmappedOwner) -> unmappedOwner }
            .mapValues { (_, entries) -> entries.map { mapper(it.drop(2)) } }

    private sealed interface TinyV2State {
        fun update(line: String): TinyV2State
        fun end(): TinyV2State
    }

    private inner class MappingState : TinyV2State {
        val classes = mutableListOf<MappedClass>()

        override fun update(line: String): TinyV2State {
            val ident = line.countStart()
            val (type, parts) = line.prepare()

            require(ident == 0) { "Invalid indent top-level" }
            require(type == "c") { "Non-class found at top level: $type" }

            return ClassState(this, parts.fixNames())
        }

        override fun end() = this
    }

    private inner class ClassState(val owner: MappingState, val names: List<String>) : TinyV2State {
        val comments = mutableListOf<String>()
        val fields = mutableListOf<MappedField>()
        val methods = mutableListOf<MappedMethod>()

        override fun update(line: String): TinyV2State {
            val ident = line.countStart()
            if (ident < 1) {
                end()
                return owner.update(line)
            }

            val (type, parts) = line.prepare()

            return when (type) {
                "f" -> FieldState(this, parts.first(), parts.drop(1).fixNames())
                "m" -> MethodState(this, parts.first(), parts.drop(1).fixNames())
                "c" -> {
                    comments += parts.joinToString("\t")
                    this
                }

                else -> error("Invalid class member type $type")
            }
        }

        override fun end(): TinyV2State {
            owner.classes += MappedClass(names, comments, fields, methods)
            return owner.end()
        }
    }

    private inner class FieldState(val owner: ClassState, val desc: String, val names: List<String>) : TinyV2State {
        val comments = mutableListOf<String>()

        override fun update(line: String): TinyV2State {
            val ident = line.countStart()
            if (ident < 2) {
                end()
                return owner.update(line)
            }

            val (type, parts) = line.prepare()
            require(type == "c") { "fields can only have comments, found type $type!" }
            comments += parts.joinToString("\t")

            return this
        }

        override fun end(): TinyV2State {
            owner.fields += MappedField(names, comments, desc)
            return owner.end()
        }
    }

    private inner class MethodState(val owner: ClassState, val desc: String, val names: List<String>) : TinyV2State {
        val comments = mutableListOf<String>()
        val parameters = mutableListOf<MappedParameter>()
        val locals = mutableListOf<MappedLocal>()

        override fun update(line: String): TinyV2State {
            val ident = line.countStart()
            if (ident < 2) {
                end()
                return owner.update(line)
            }

            val (type, parts) = line.prepare()
            when (type) {
                "c" -> comments += parts.joinToString("\t")
                "p" -> {
                    val (index, names) = parts.splitFirst()
                    parameters += MappedParameter(names, index.toIntOrNull() ?: error("Invalid index $index"))
                }

                "v" -> {
                    val (idx, offset, lvtIndex) = parts.take(3).map {
                        it.toIntOrNull() ?: error("Invalid index $it for local")
                    }

                    locals += MappedLocal(idx, offset, lvtIndex, parts.drop(3))
                }

                else -> error("Illegal type in method $type")
            }

            return this
        }

        override fun end(): TinyV2State {
            owner.methods += MappedMethod(names, comments, desc, parameters, locals)
            return owner.end()
        }
    }

    private fun String.prepare() = trimIndent().parts().splitFirst()

    private fun <T> List<T>.splitFirst() = first() to drop(1)
    private fun String.countStart(sequence: String = "\t") =
        windowedSequence(sequence.length, sequence.length).takeWhile { it == sequence }.count()

    private fun String.parts() = split('\t')
}

data class SRGMappings(override val classes: List<MappedClass>, val isExtended: Boolean) : Mappings {
    override val namespaces = listOf("official", "named")
}

fun SRGMappings.asSimpleRemapper() = asSimpleRemapper(namespaces[0], namespaces[1])

object SRGMappingsFormat : MappingsFormat<SRGMappings> by BasicSRGParser(false)
object XSRGMappingsFormat : MappingsFormat<SRGMappings> by BasicSRGParser(true)

private class BasicSRGParser(private val isExtended: Boolean) : MappingsFormat<SRGMappings> {
    private val entryTypes = setOf("CL", "FD", "MD", "PK")
    override fun detect(lines: List<String>): Boolean {
        if (!lines.all { it.substringBefore(':') in entryTypes }) return false
        return lines.find { it.startsWith("FD:") }?.let { l -> l.split(" ").size > 3 == isExtended } ?: true
    }

    override fun parse(lines: List<String>): SRGMappings {
        val parted = lines.map { it.split(" ") }
        val fields = parted.collect("FD") { parts ->
            val from = parts[0]
            val to = parts[if (isExtended) 2 else 1]

            MappedField(
                names = listOf(from.substringAfterLast('/'), to.substringAfterLast('/')),
                comments = listOf(),
                desc = if (isExtended) parts[1] else null
            )
        }

        val methods = parted.collect("MD") { (from, fromDesc, to) ->
            MappedMethod(
                names = listOf(from.substringAfterLast('/'), to.substringAfterLast('/')),
                comments = listOf(),
                desc = fromDesc,
                parameters = listOf(),
                variables = listOf()
            )
        }

        val classEntries = parted.filter { (type) -> type == "CL:" }

        // Make sure we do not forget about orphaned ones
        // (sometimes mappings do not specify mappings for the class but they do for some entries)
        val missingClasses = methods.keys + fields.keys - classEntries.map { (_, from) -> from }.toSet()
        val classes = classEntries.map { (_, from, to) ->
            MappedClass(
                names = listOf(from, to),
                comments = listOf(),
                fields = fields[from] ?: listOf(),
                methods = methods[from] ?: listOf()
            )
        } + missingClasses.map { name ->
            MappedClass(
                names = listOf(name, name),
                comments = listOf(),
                fields = fields[name] ?: listOf(),
                methods = methods[name] ?: listOf()
            )
        }

        return SRGMappings(classes, isExtended)
    }

    private inline fun <T> List<List<String>>.collect(type: String, mapper: (List<String>) -> T) =
        filter { (t) -> t == "$type:" }
            .groupBy { (_, from) -> from.substringBeforeLast('/') }
            .mapValues { (_, entries) -> entries.map { mapper(it.drop(1)) } }
}

class AccessWideningVisitor(parent: ClassVisitor) : ClassVisitor(ASM9, parent) {
    private fun Int.widen() = this and (ACC_PRIVATE or ACC_PROTECTED).inv() or ACC_PUBLIC
    private fun Int.removeFinal() = this and ACC_FINAL.inv()

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        super.visit(version, access.widen().removeFinal(), name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor = super.visitMethod(access.widen().removeFinal(), name, descriptor, signature, exceptions)

    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?
    ): FieldVisitor = super.visitField(access.widen(), name, descriptor, signature, value)
}

class LambdaAwareRemapper(parent: ClassVisitor, remapper: Remapper) : ClassRemapper(ASM9, parent, remapper) {
    override fun createMethodRemapper(parent: MethodVisitor) = object : MethodRemapper(ASM9, parent, remapper) {
        override fun visitInvokeDynamicInsn(name: String, descriptor: String, handle: Handle, vararg args: Any) {
            val remappedName = if (
                handle.owner == "java/lang/invoke/LambdaMetafactory" &&
                (handle.name == "metafactory" || handle.name == "altMetafactory")
            ) {
                // Lambda, so we need to rename it... weird edge case, maybe ASM issue?
                // LambdaMetafactory just causes an IncompatibleClassChangeError if the lambda is invalid
                // Does it assume correct compile time? odd.
                remapper.mapMethodName(
                    Type.getReturnType(descriptor).internalName,
                    name,
                    (args.first() as Type).descriptor
                )
            } else name

            parent.visitInvokeDynamicInsn(
                /* name = */ remappedName,
                /* descriptor = */ remapper.mapMethodDesc(descriptor),
                /* bootstrapMethodHandle = */ remapper.mapValue(handle) as Handle,
                /* ...bootstrapMethodArguments = */ *args.map { remapper.mapValue(it) }.toTypedArray()
            )
        }
    }
}

class MappingsRemapper(
    mappings: Mappings,
    from: String,
    to: String,
    private val loader: (name: String) -> ByteArray?
) : Remapper() {
    private val map = mappings.asASMMapping(from, to)

    override fun map(internalName: String): String = map[internalName] ?: internalName
//    override fun mapInnerClassName(name: String, ownerName: String?, innerName: String?) = map(name)

    override fun mapMethodName(owner: String, name: String, desc: String): String {
        if (name == "<init>" || name == "<clinit>") return name

        // Source: https://github.com/FabricMC/tiny-remapper/blob/d14e8f99800e7f6f222f820bed04732deccf5109/src/main/java/net/fabricmc/tinyremapper/AsmRemapper.java#L74
        return if (desc.startsWith("(")) walk(owner, name) { map["$it.$name$desc"] }
        else mapFieldName(owner, name, desc)
    }

    override fun mapFieldName(owner: String, name: String, desc: String) = walk(owner, name) { map["$it.$name"] }

    override fun mapRecordComponentName(owner: String, name: String, desc: String) =
        mapFieldName(owner, name, desc)

    private inline fun walk(
        owner: String,
        name: String,
        applicator: (owner: String) -> String?
    ): String {
        val queue = ArrayDeque<String>()
        val seen = hashSetOf<String>()
        queue.addLast(owner)

        while (queue.isNotEmpty()) {
            val curr = queue.removeLast()
            val new = applicator(curr)
            if (new != null) return new

            val bytes = loader(curr) ?: continue
            val reader = ClassReader(bytes)

            reader.superName?.let { if (seen.add(it)) queue.addLast(it) }
            queue += reader.interfaces.filter { seen.add(it) }
        }

        return name
    }
}

fun remapJar(
    mappings: Mappings,
    input: File,
    output: File,
    from: String = "official",
    to: String = "named"
) {
    JarFile(input).use { jar ->
        JarOutputStream(output.outputStream()).use { out ->
            val (classes, resources) = jar.entries().asSequence().partition { it.name.endsWith(".class") }

            fun write(name: String, bytes: ByteArray) {
                out.putNextEntry(JarEntry(name))
                out.write(bytes)
            }

            resources.forEach { write(it.name, jar.getInputStream(it).readBytes()) }

            val cache = hashMapOf<String, ByteArray?>()
            val remapper = MappingsRemapper(
                mappings, from, to,
                loader = { name ->
                    cache.getOrPut(name) { jar.getJarEntry("$name.class")?.let { jar.getInputStream(it).readBytes() } }
                }
            )

            classes.forEach { entry ->
                val reader = ClassReader(jar.getInputStream(entry).readBytes())
                val writer = ClassWriter(reader, 0)
                reader.accept(LambdaAwareRemapper(writer, remapper), 0)

                write("${remapper.map(reader.className)}.class", writer.toByteArray())
            }
        }
    }
}