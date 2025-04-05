import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta11"
    id("com.modrinth.minotaur") version "2.+"
    id("io.papermc.hangar-publish-plugin") version "0.1.3"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.compileJava {
    options.release.set(21)
}

group = "net.thenextlvl.worlds"
version = "2.1.3"

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.foliaDevBundle("1.21.4-R0.1-SNAPSHOT")

    implementation(project(":worlds-api"))
    implementation(project(":per-worlds"))
    implementation("net.thenextlvl.core:i18n:1.0.21")
    implementation("net.thenextlvl.core:paper:2.0.4")
}

tasks.shadowJar {
    archiveBaseName.set("worlds")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    relocate("org.bstats", "net.thenextlvl.worlds.bstats")
}

paper {
    name = "Worlds"
    main = "net.thenextlvl.worlds.WorldsPlugin"
    apiVersion = "1.21"
    description = "Create, delete and manage your worlds"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    website = "https://thenextlvl.net"
    authors = listOf("NonSwag")
    foliaSupported = true
    provides = listOf("PerWorlds")

    permissions {
        register("worlds.commands.admin") {
            this.children = listOf(
                "worlds.command.clone",
                "worlds.command.create",
                "worlds.command.delete",
                "worlds.command.import",
                "worlds.command.info",
                "worlds.command.link",
                "worlds.command.list",
                "worlds.command.load",
                "worlds.command.save",
                "worlds.command.save-all",
                "worlds.command.save-off",
                "worlds.command.save-on",
                "worlds.command.setspawn",
                "worlds.command.spawn",
                "worlds.command.teleport",
                "worlds.command.unload",
            )
        }
        register("perworlds.command") { children = listOf("worlds.command") }
        register("perworlds.command.group") { children = listOf("perworlds.command") }
        register("worlds.command.group") { children = listOf("perworlds.command.group") }

        register("worlds.commands.link") {
            this.children = listOf(
                "worlds.command.link.create",
                "worlds.command.link.delete",
                "worlds.command.link.list"
            )
        }
        register("worlds.command.link.create") {
            this.children = listOf("worlds.command.link")
        }
        register("worlds.command.link.delete") {
            this.children = listOf("worlds.command.link")
        }
        register("worlds.command.link.list") {
            this.children = listOf("worlds.command.link")
        }

        register("worlds.command.link") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.clone") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.create") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.delete") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.import") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.info") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.list") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.load") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.save") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.save-all") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.save-off") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.save-on") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.setspawn") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.spawn") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.teleport") {
            this.children = listOf("worlds.command")
        }
        register("worlds.command.unload") {
            this.children = listOf("worlds.command")
        }
    }
}

val versionString: String = project.version as String
val isRelease: Boolean = !versionString.contains("-pre")

val versions: List<String> = (property("gameVersions") as String)
    .split(",")
    .map { it.trim() }

hangarPublish { // docs - https://docs.papermc.io/misc/hangar-publishing
    publications.register("plugin") {
        id.set("Worlds")
        version.set(versionString)
        channel.set(if (isRelease) "Release" else "Snapshot")
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms.register(Platforms.PAPER) {
            jar.set(tasks.shadowJar.flatMap { it.archiveFile })
            platformVersions.set(versions)
        }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("gBIw3Gvy")
    versionType = if (isRelease) "release" else "beta"
    uploadFile.set(tasks.shadowJar)
    gameVersions.set(versions)
    loaders.add("paper")
    loaders.add("folia")
}