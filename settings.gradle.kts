plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}

rootProject.name = "worlds"
include("per-worlds")
include("per-worlds-api")
include("source-generator")
include("worlds")
include("worlds-api")
