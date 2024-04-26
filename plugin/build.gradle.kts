import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

group = rootProject.group
version = "1.1.6"

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.thenextlvl.net/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("org.projectlombok:lombok:1.18.32")
    compileOnly("net.thenextlvl.core:annotations:2.0.1")
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("org.incendo:cloud-paper:2.0.0-beta.4")
    implementation("org.incendo:cloud-processors-confirmation:1.0.0-beta.1")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.4")

    implementation(project(":api"))
    implementation("net.thenextlvl.core:nbt:1.3.9")
    implementation("net.thenextlvl.core:files:1.0.3")
    implementation("net.thenextlvl.core:i18n:1.0.13")
    implementation("net.thenextlvl.core:paper:1.2.3")
    implementation("net.thenextlvl.core:adapters:1.0.6")

    annotationProcessor("org.projectlombok:lombok:1.18.32")
}


tasks.shadowJar {
    relocate("org.bstats", "net.thenextlvl.worlds.bstats")
    archiveBaseName.set("worlds")
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

val versionString: String = project.version as String
val isRelease: Boolean = !versionString.contains("-pre")

hangarPublish { // docs - https://docs.papermc.io/misc/hangar-publishing
    publications.register("plugin") {
        id.set("Worlds")
        version.set(project.version as String)
        channel.set(if (isRelease) "Release" else "Snapshot")
        if (extra.has("HANGAR_API_TOKEN"))
            apiKey.set(extra["HANGAR_API_TOKEN"] as String)
        else apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                val versions: List<String> = (property("paperVersion") as String)
                    .split(",")
                    .map { it.trim() }
                platformVersions.set(versions)
            }
        }
    }
}