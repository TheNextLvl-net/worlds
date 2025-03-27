import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-beta9"
    id("io.papermc.hangar-publish-plugin") version "0.1.3"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.compileJava {
    options.release.set(21)
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")

    api("org.bstats:bstats-bukkit:3.1.0")

    api("net.thenextlvl.core:i18n:1.0.20")
    api("net.thenextlvl.core:paper:2.0.3")

    compileOnlyApi(project(":api"))
}

paper {
    name = "PerWorlds"
    main = "net.thenextlvl.perworlds.WorldsPlugin"
    apiVersion = "1.21"
    description = "Per-world customization for gameplay and settings"
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    website = "https://thenextlvl.net"
    authors = listOf("NonSwag")
    foliaSupported = true
    serverDependencies {
        register("Worlds") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
    }
}

val versionString: String = project.version as String
val isRelease: Boolean = !versionString.contains("-pre")

val versions: List<String> = (property("gameVersions") as String)
    .split(",")
    .map { it.trim() }

hangarPublish { // docs - https://docs.papermc.io/misc/hangar-publishing
    publications.register("per-worlds") {
        id.set("PerWorlds")
        version.set(versionString)
        channel.set(if (isRelease) "Release" else "Snapshot")
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms.register(Platforms.PAPER) {
            jar.set(tasks.shadowJar.flatMap { it.archiveFile })
            platformVersions.set(versions)
        }
    }
}