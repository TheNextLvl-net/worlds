import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

group = rootProject.group
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.28")
    compileOnly("net.thenextlvl.core:annotations:2.0.0")
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    implementation("cloud.commandframework:cloud-paper:1.8.4")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.8.4")

    implementation(project(":api"))
    implementation("net.thenextlvl.core:nbt:1.2.0")
    implementation("net.thenextlvl.core:api:4.0.1")
    implementation("net.thenextlvl.core:i18n:1.0.7")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    annotationProcessor("org.projectlombok:lombok:1.18.28")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


tasks.shadowJar {
    relocate("org.bstats", "net.thenextlvl.tweaks.bstats")
    minimize()
}

paper {
    name = "Worlds"
    main = "net.thenextlvl.worlds.Worlds"
    apiVersion = "1.19"
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    website = "https://thenextlvl.net"
    authors = listOf("NonSwag")

    permissions {
        register("worlds.commands.world") {
            this.children = listOf(
                    "worlds.command.world.create",
                    "worlds.command.world.delete",
                    "worlds.command.world.export",
                    "worlds.command.world.import",
                    "worlds.command.world.info",
                    "worlds.command.world.list",
                    "worlds.command.world.setspawn",
                    "worlds.command.world.teleport"
            )
        }
        register("worlds.commands.link") {
            this.children = listOf(
                    "worlds.command.link.create",
                    "worlds.command.link.delete",
                    "worlds.command.link.list"
            )
        }
    }
}