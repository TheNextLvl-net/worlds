repositories {
    maven("https://maven.canvasmc.io/releases")
    maven("https://maven.canvasmc.io/snapshots")
}

dependencies {
    // no folia dev bundle for 26.3 yet, canvas ships the folia internals
    paperweight.devBundle("io.canvasmc.canvas", "26.3.build.+")
}
