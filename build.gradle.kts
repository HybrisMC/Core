import dev.hybrismc.meta.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import java.util.jar.JarFile
import kotlin.io.path.exists

allprojects {
    group = "dev.hybrismc"
    version = "0.1"
}

data class MappingEntry<T : Mapped>(val mappings: Mappings, val entry: T)
fun MappedClass.named(mappings: Mappings) = names[mappings.namespace("named")]
fun MappedClass.original(mappings: Mappings) = names[mappings.namespace("official")]
fun MappingEntry<*>.named() = entry.names[mappings.namespace("named")]
fun MappingEntry<*>.original() = entry.names[mappings.namespace("official")]
fun MappingEntry<MappedClass>.methods() = entry.methods.map { MappingEntry(mappings, it) }
fun MappingEntry<MappedClass>.fields() = entry.fields.map { MappingEntry(mappings, it) }

fun Type.remap(mappings: Mappings): Type = when (sort) {
    Type.ARRAY -> Type.getObjectType("[${elementType.remap(mappings).internalName}")
    Type.OBJECT -> Type.getObjectType(
        mappings.classes.find { it.original(mappings) == internalName }?.named(mappings) ?: internalName
    )

    else -> this
}

fun Type.remapDesc(mappings: Mappings) = when (sort) {
    Type.METHOD -> Type.getMethodType(
        returnType.remap(mappings),
        *argumentTypes.map { it.remap(mappings) }.toTypedArray()
    )
    else -> remap(mappings)
}

fun findForTarget(target: String) = allMappings().mapValues {
    it.value.classes.find { c -> c.names.last() == target }?.let { c -> MappingEntry(it.value, c) }
}.partitionValuesNotNull()

tasks.register("findSimilarities") {
    doLast {
        val target = (findProperty("target") ?: throw GradleException("Specify a -Ptarget!")).toString()
        val (has, doesNot) = findForTarget(target)

        println("Checking for ${has.map { (a) -> a }}, $doesNot do not have $target")

        fun <T : MappingEntry<*>> Map<MappingEntry<MappedClass>, List<T>>.filterAll(
            cond: (T, T) -> Boolean = { _, _ -> true }
        ) = values.first().filter { e ->
            all { it.value.any { o -> o.named() == e.named() && cond(o, e) } }
        }

        val classes = has.map { it.value }
        val methods = classes.associateWith { it.methods() }.filterAll { a, b ->
            Type.getMethodType(a.entry.desc).remap(a.mappings) == Type.getMethodType(b.entry.desc).remap(b.mappings)
        }

        val fields = classes.associateWith { it.fields() }.filterAll { a, b ->
            Type.getType(a.entry.desc).remap(a.mappings) == Type.getType(b.entry.desc).remap(b.mappings)
        }

        val (targetGame, intermediateTarget) = has.toList()
            .sortedBy { it.first.substringAfter('.') }
            .map { (a, b) -> gameJarOf(a) to b }
            .first { (a) -> a.exists() }

        println("Using $targetGame as reference")

        val targetNode = JarFile(targetGame.toFile()).use { jar ->
            val mappedTarget = intermediateTarget.original()
            val node = ClassNode()

            ClassReader(
                jar.getInputStream(
                    jar.entries().asSequence().first { it.name.removeSuffix(".class") == mappedTarget }
                ).readBytes()
            ).accept(node, 0)

            node
        }

        fun <T : MappingEntry<*>> List<T>.splitStatic(
            intermediate: List<T>,
            selector: (String) -> Int
        ) = partition { e ->
            selector(
                intermediate.find { it.named() == e.named() }?.original() ?: return@partition false
            ) and Opcodes.ACC_STATIC != 0
        }

        val (staticMethods, instanceMethods) = methods.splitStatic(intermediateTarget.methods()) { orig ->
            targetNode.methods.find { it.name == orig }?.access ?: 0
        }

        val (staticFields, instanceFields) = fields.splitStatic(intermediateTarget.fields()) { orig ->
            targetNode.fields.find { it.name == orig }?.access ?: 0
        }

        fun Type.format(mappings: Mappings): String = when (sort) {
            Type.ARRAY -> "Array<${elementType.format(mappings)}>"
            Type.INT -> "Int"
            Type.DOUBLE -> "Double"
            Type.FLOAT -> "Float"
            Type.SHORT -> "Short"
            Type.BOOLEAN -> "Boolean"
            Type.BYTE -> "Byte"
            Type.CHAR -> "Char"
            Type.LONG -> "Long"
            Type.VOID -> "Unit"
            Type.OBJECT -> remap(mappings).internalName.substringAfterLast('/')
            else -> error("Illegal type sort $sort")
        }

        fun formatField(name: String, type: Type, mappings: Mappings) = "val $name: ${type.format(mappings)}"
        fun MappingEntry<MappedField>.format() = formatField(named(), Type.getType(entry.desc), mappings)

        fun MappingEntry<MappedMethod>.format(): String {
            val named = named()
            val mType = Type.getMethodType(entry.desc)
            val argumentTypes = mType.argumentTypes
            val returnType = mType.returnType

            if (named.startsWith("get") && argumentTypes.isEmpty()) return formatField(
                name = named.drop(3).replaceFirstChar { it.lowercaseChar() },
                type = returnType,
                mappings = mappings
            )

            val byIndex = entry.parameters.map { MappingEntry(mappings, it) }.associateBy { it.entry.index }
            val indexToActual = argumentTypes.scan(byIndex.keys.minOrNull() ?: 0) { acc, curr -> acc + curr.size }
            val parameters = List(argumentTypes.size) {
                "${byIndex[indexToActual[it]]?.named() ?: "arg$it"}: ${argumentTypes[it].format(mappings)}"
            }.joinToString()

            val formattedReturn = if (returnType.sort == Type.VOID) "" else ": ${returnType.format(mappings)}"
            val formattedName = if (named == "<init>") "construct" else named
            val annotation = if (named == "<init>") "@ConstructorAccess\n" else ""
            return """${annotation}fun $formattedName($parameters)$formattedReturn"""
        }

        val indent = "    "
        fun List<String>.indent() = joinToString(separator = "\n") { indent + it }

        fun buildInterface(
            name: String,
            static: Boolean,
            methods: List<MappingEntry<MappedMethod>>,
            fields: List<MappingEntry<MappedField>>,
            extra: String? = null,
        ) = """interface ${if (static) "Static : StaticAccessor<$name>" else name} {
${fields.map { it.format() }.sorted().indent()}
${methods.map { it.format() }.sorted().indent()}${if (extra != null) "\n" + extra.lines().indent() else ""}
}"""

        val shortName = target.substringAfterLast('/')
        val accessName = shortName.replaceFirstChar { it.lowercaseChar() } + "Access"
        val anno = "@Named(\"$target\")\n"
        println(
            "val $accessName = accessor<_, $shortName.Static>()\n\n$anno" + buildInterface(
                name = shortName,
                static = false,
                methods = instanceMethods,
                fields = instanceFields,
                extra = "\n" + buildInterface(
                    name = shortName,
                    static = true,
                    methods = staticMethods,
                    fields = staticFields
                ) + "\n\ncompanion object : Static by $accessName.static()"
            )
        )
    }
}

fun <K, V> Map<K, V?>.partitionValuesNotNull(): Pair<Map<K, V>, List<K>> {
    val result = mutableMapOf<K, V>()
    val nullKeys = mutableListOf<K>()
    forEach { (k, v) -> if (v == null) nullKeys += k else result += k to v }
    return result to nullKeys
}