plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("0.9.0")
}

rootProject.name = "worlds"
include("per-worlds")
include("per-worlds-api")
include("worlds")
include("worlds-api")
