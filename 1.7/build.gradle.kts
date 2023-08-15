plugins {
    id("kotlin-convention")
    id("toolchain")
}

repositories {
    mavenCentral()
    maven("https://repo.legacyfabric.net/repository/legacyfabric")
}

toolchain { minecraftVersion.set(dev.hybrismc.meta.MinecraftVersion.V1_7) }

dependencies {
    mappings("net.legacyfabric:yarn:1.7.10+build.514:mergedv2")
}