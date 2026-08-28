plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23" apply false
}

dependencies {
    compileOnly("dev.faststats.metrics:bukkit:0.29.4")
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly(project(":api"))
}

subprojects {
    apply(plugin = "io.papermc.paperweight.userdev")

    dependencies {
        "compileOnly"("dev.faststats.metrics:bukkit:0.29.4")
        "compileOnly"(project(":api"))
        "compileOnlyApi"(project(":version-specifics"))
    }
}
