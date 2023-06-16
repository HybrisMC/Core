plugins {
    id("kotlin-convention")
    id("toolchain")
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
}

toolchain { minecraftVersion.set(dev.hybrismc.meta.MinecraftVersion.V1_16_1) }

dependencies {
    mappings("net.fabricmc:yarn:1.16.1+build.9:mergedv2")
}