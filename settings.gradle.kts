rootProject.name = "hybris-core"

includeBuild("meta") {
    dependencySubstitution {
        substitute(module("dev.hybrismc:meta")).using(project(":"))
    }
}

includeBuild("class-util") {
    dependencySubstitution {
        substitute(module("com.grappenmaker:nasty-jvm-util")).using(project(":"))
    }
}

include("core", "1.7", "1.8", "1.12", "1.16.1", "1.19.4", "1.20")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}