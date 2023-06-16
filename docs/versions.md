# Version Modules

Version modules are a way of splitting up the code in order to be able to write
code for specific versions of the game. They also allow you to launch the game
in a development environment and independently setup decompilation environments
that mirror the runtime. These are handles by the [toolchain](toolchain.md). A
typical version module looks like this:

```kt
plugins {
    // Applies the Kotlin plugins in one line
    id("kotlin-convention")

    // Applies the toolchain
    id("toolchain")
}

repositories {
    mavenCentral()

    // To download and use mappings, you need to declare a maven repository that defines them
    maven("<mapping repository>")
}

// Declares the version to use for this module
toolchain { minecraftVersion.set(dev.hybrismc.meta.MinecraftVersion.SomeVersion) }

// Declares the mapping dependency for the toolchain
dependencies {
    mappings("net.legacyfabric:yarn:1.8.9+build.456:mergedv2")
}
```

In the future, we need a mechanism that allows calling these modules based on
the current version of the game, maybe through the toolchain as well.
