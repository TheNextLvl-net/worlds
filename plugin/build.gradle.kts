import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

group = "net.thenextlvl.worlds"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.26")
    compileOnly("net.thenextlvl.core:annotations:1.0.0")
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    compileOnly("cloud.commandframework:cloud-paper:1.8.3")
    compileOnly("cloud.commandframework:cloud-minecraft-extras:1.8.3")

    implementation(project(":api"))
    implementation("net.thenextlvl.core:api:3.1.10")

    annotationProcessor("org.projectlombok:lombok:1.18.26")
}


tasks {
    shadowJar {
        minimize()
    }
}

bukkit {
    name = "Worlds"
    main = "net.thenextlvl.worlds.Worlds"
    apiVersion = "1.19"
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    website = "https://thenextlvl.net"
    authors = listOf("NonSwag")
    libraries = listOf(
            "cloud.commandframework:cloud-paper:1.8.3",
            "cloud.commandframework:cloud-minecraft-extras:1.8.3"
    )
}