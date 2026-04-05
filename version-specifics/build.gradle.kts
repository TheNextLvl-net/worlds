plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
}

dependencies {
    compileOnly("dev.faststats.metrics:core:0.21.0")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
}

subprojects {
    apply(plugin = "io.papermc.paperweight.userdev")
    apply(plugin = "java-library")

    dependencies {
        "compileOnly"("dev.faststats.metrics:core:0.21.0")
        "compileOnly"(project(":api"))
        "compileOnlyApi"(project(":version-specifics"))
    }
}
