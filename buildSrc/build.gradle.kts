plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.20")
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.8.20")
    implementation("dev.hybrismc:meta")
}

gradlePlugin {
    plugins {
        create("toolchain") {
            id = "toolchain"
            implementationClass = "ToolchainPlugin"
        }
    }
}