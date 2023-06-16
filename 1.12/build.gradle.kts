plugins {
    id("kotlin-convention")
    id("toolchain")
}

repositories {
    mavenCentral()
    maven("https://repo.legacyfabric.net/repository/legacyfabric")
}

toolchain { minecraftVersion.set(dev.hybrismc.meta.MinecraftVersion.V1_12) }

dependencies {
    mappings("net.legacyfabric:yarn:1.12.2+build.481:mergedv2")
}