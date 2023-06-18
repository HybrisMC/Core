dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "meta"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}