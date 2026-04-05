plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}

rootProject.name = "worlds"
include("api")
include("version-specifics")
include("version-specifics:v1.21.10")
include("version-specifics:v1.21.11")
include("version-specifics:v1.21.4")
include("version-specifics:v1.21.8")
include("version-specifics:v26.1.1")
include("plugin")