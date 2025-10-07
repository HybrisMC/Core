// Seperate project because they need to be included in various locations

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
}

group = "dev.hybrismc"
version = "0.1"

repositories {
    mavenCentral()
}

kotlin { jvmToolchain(16) }

dependencies {
    api(libs.bundles.asm.all)
    api("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.5.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("io.ktor:ktor-client-logging-jvm:2.3.0")
    ktorClientAPI("core", "cio", "content-negotiation")
    ktorAPI("serialization-kotlinx-json")
}

fun DependencyHandlerScope.ktorClientAPI(vararg names: String) =
    names.forEach { ktorAPI("client-$it") }

fun DependencyHandlerScope.ktorAPI(name: String) = api(
    group = "io.ktor",
    name = "ktor-$name",
    version = "2.3.0"
)