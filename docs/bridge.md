# Bridge

The bridge is a mechanism that allows us to access game code, independent of
what version the game is on. A utility task in the main build script extracts
mapping and class info in order to generate interfaces that declare the
structure of a Minecraft class that is present for every version module. The
implementation of these interfaces differ per the type of accessor.  For
constructors and static methods, a static accessor is used: this is implemented
through an external, newly generated class, and loaded through the game loader,
using a class definition inside of the `GenerationLoader`. The other type of
accessor is the virtual or instance accessor. This is implemented through
declaring additional methods onto the "bridged" class, but only if they are
missing / not identical. Do note that one issue here is that there can be
multiple methods with the same arguments but a different return type. For
ordinary method calls, this is absolutely no issue, it is not allowed by JLS
though. However, even if a method is reflected for whatever reason (reflection
does not take into account what the return type is, as it looks up the method
by only its arguments), reflection always takes the first occurrence and
therefore the bridge will not interfere.

The bridge is part of the transformation chain, and therefore the target
accessors must be declared before anything is transformed. A typical bridge
looks like this:

```kt
// Declares the accessor generation
val identifierAccess = accessor<_, Identifier.Static>()

// Defines what MC class to target
@Named("net/minecraft/util/Identifier")
interface Identifier {
    // Declares the instance accessors
    val namespace: String
    val path: String

    // Declares the static accessors
    interface Static : StaticAccessor<Identifier> {
        // Constructors are static, and return the virtual type,
        // and have to be annotated with @ConstructorAccess,
        // and can have any name
        @ConstructorAccess
        fun construct(name: String): Identifier

        @ConstructorAccess
        fun construct(namespace: String, path: String): Identifier
    }

    // This allows to access the static methods as if they were actually static
    // Static interface methods are not allowed, so this is a clever workaround
    companion object : Static by identifierAccess.static()
}

// You can now call this accessor later:
Identifier.construct("somepath").path
```

Another type of accessor lives in the `class-util` library. It is a different
type of accessor, since it does not directly resolve methods and fields.
Instead, you have to declare the methods inside of a finder (these need to be
documented, TODO). This allows you to match on more metadata than a "Direct
Accessor" can, but require more code. The code generation is also external and
uses a delegation pattern, since it does not touch the target classes.  An
example could look like this:

```kt
// Declares finders, which in turn declare methods to match on
val textComponent = findNamedClass("net/minecraft/text/Text")
val textComponentSerializer = findNamedClass("net/minecraft/text/Text\$Serializer") {
    methods {
        "create" {
            arguments hasExact listOf(asmTypeOf<String>())
            method.isStatic()
            method match { it.method.returnType.internalName.endsWith("Text") }
        }
    }
}

// Declares the generation of accessors, notice the delegation
val textComponentAccess by globalAccessorRegistry.accessor<_, TextComponent.Static>(textComponent)
val tcsAccess by globalAccessorRegistry.accessor<_, TextComponent.Serializer.Static>(textComponentSerializer)

// Defines the type
interface TextComponent : InstanceAccessor {
    // Serializer is actually an inner type in TextComponent
    interface Serializer : InstanceAccessor {
        interface Static : com.grappenmaker.jvmutil.StaticAccessor<Serializer> {
            fun create(json: String): TextComponent
        }

        companion object : Static by tcsAccess.static
    }

    interface Static : com.grappenmaker.jvmutil.StaticAccessor<TextComponent>
    companion object : Static by textComponentAccess.static
}
```
