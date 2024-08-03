import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("io.github.goooler.shadow") version "8.1.7"
    id("com.modrinth.minotaur") version "2.+"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.compileJava {
    options.release.set(21)
}

group = project(":api").group
version = project(":api").version

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.thenextlvl.net/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.32")
    compileOnly("net.thenextlvl.core:annotations:2.0.1")

    implementation("org.bstats:bstats-bukkit:3.0.2")

    implementation(project(":api"))
    implementation("net.thenextlvl.core:nbt:1.4.2")
    implementation("net.thenextlvl.core:files:1.0.5")
    implementation("net.thenextlvl.core:i18n:1.0.18")
    implementation("net.thenextlvl.core:paper:1.4.1")
    implementation("net.thenextlvl.core:adapters:1.0.9")

    annotationProcessor("org.projectlombok:lombok:1.18.32")
}


tasks.shadowJar {
    relocate("org.bstats", "net.thenextlvl.worlds.bstats")
    archiveBaseName.set("worlds")
    // minimize() // breaks cloud
}

paper {
    name = "Worlds"
    main = "net.thenextlvl.worlds.WorldsPlugin"
    apiVersion = "1.20"
    description = "Create, delete and manage your worlds"
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
}