plugins {
    id("kotlin-convention")
    id("toolchain")
}

repositories {
    mavenCentral()
    maven("https://repo.legacyfabric.net/repository/legacyfabric")
}

toolchain { minecraftVersion.set(dev.hybrismc.meta.MinecraftVersion.V1_8) }

dependencies {
    mappings("net.legacyfabric:yarn:1.8.9+build.456:mergedv2")
}