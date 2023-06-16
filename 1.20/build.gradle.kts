plugins {
    id("kotlin-convention")
    id("toolchain")
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
}

toolchain { minecraftVersion.set(dev.hybrismc.meta.MinecraftVersion.V1_20) }

dependencies {
    mappings("net.fabricmc:yarn:1.20.1+build.2:mergedv2")
}