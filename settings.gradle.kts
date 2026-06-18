plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}

rootProject.name = "worlds"
include("api")
include("version-specifics")
include("version-specifics:v26.1.2")
include("version-specifics:v26.2")
include("plugin")