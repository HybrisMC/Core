plugins {
    id("kotlin-convention")
    id("toolchain")
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
}

toolchain { minecraftVersion.set(dev.hybrismc.meta.MinecraftVersion.V1_19) }

dependencies {
    mappings("net.fabricmc:yarn:1.19.4+build.2:mergedv2")
}