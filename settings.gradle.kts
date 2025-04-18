plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("0.10.0")
}

rootProject.name = "worlds"
include("per-worlds")
include("per-worlds-api")
include("source-generator")
include("worlds")
include("worlds-api")
