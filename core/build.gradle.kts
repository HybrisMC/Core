plugins {
    id("kotlin-convention")
}

repositories {
    mavenCentral()
    spongeMaven()
}

dependencies {
    implementation("dev.hybrismc:meta")
    implementation("com.grappenmaker:nasty-jvm-util") {
        capabilities {
            requireCapability("com.grappenmaker:nasty-jvm-util-reflect")
        }
    }

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.19.0")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.19.0")
    runtimeOnly("net.minecrell:terminalconsoleappender:1.3.0") {
        exclude(group = "org.apache.logging.log4j")
    }

    runtimeOnly("org.jline:jline-terminal:3.23.0")
    runtimeOnly("org.jline:jline-reader:3.23.0")
    runtimeOnly("org.jline:jline-terminal-jansi:3.23.0")

    api("org.spongepowered:mixin:0.8.5")
    implementation("com.google.guava:guava:31.0-jre")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks {
    processResources {
        expand("version" to version)
    }
}