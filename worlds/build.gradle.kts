import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("com.gradleup.shadow")
    id("com.modrinth.minotaur")
    id("de.eldoria.plugin-yml.paper")
    id("io.papermc.hangar-publish-plugin")
    id("io.papermc.paperweight.userdev")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.compileJava {
    options.release.set(21)
}

group = "net.thenextlvl.worlds"
version = "3.1.0"

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.foliaDevBundle("1.21.5-R0.1-SNAPSHOT")
    //paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")

    implementation(project(":worlds-api"))
    implementation(project(":per-worlds"))
    implementation("net.thenextlvl.core:i18n:3.2.0")
    implementation("net.thenextlvl.core:paper:2.2.1")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(platform("org.junit:junit-bom:5.13.1"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showCauses = true
        showExceptions = true
    }
}

tasks.shadowJar {
    archiveBaseName.set("worlds")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    relocate("org.bstats", "net.thenextlvl.worlds.bstats")
}

paper {
    name = "Worlds"
    main = "net.thenextlvl.worlds.WorldsPlugin"
    apiVersion = "1.21.5"
    description = "Create, delete and manage your worlds"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    website = "https://thenextlvl.net"
    authors = listOf("NonSwag")
    foliaSupported = true
    provides = listOf("PerWorlds")

    permissions {
        project(":per-worlds").paper.permissions.forEach { add(it) }

        register("worlds.commands.admin") {
            children = listOf("worlds.admin")
            description = "Backwards compat for the new worlds.admin permission"
        }

        register("worlds.admin") {
            description = "Allows access to all world commands"
            children = listOf(
                "perworlds.admin",
                "worlds.command.backup",
                "worlds.command.clone",
                "worlds.command.create",
                "worlds.command.delete",
                "worlds.command.import",
                "worlds.command.info",
                "worlds.command.list",
                "worlds.command.load",
                "worlds.command.save",
                "worlds.command.save-all",
                "worlds.command.save-off",
                "worlds.command.save-on",
                "worlds.command.seed",
                "worlds.command.setspawn",
                "worlds.command.spawn",
                "worlds.command.teleport",
                "worlds.command.unload",
                "worlds.commands.link"
            )
        }

        register("perworlds.command") { children = listOf("worlds.command") }

        register("worlds.commands.link") {
            description = "Grants access to all world link commands"
            children = listOf(
                "worlds.command.link.create",
                "worlds.command.link.remove",
                "worlds.command.link.list"
            )
        }
        register("worlds.command.link.create") { children = listOf("worlds.command.link") }
        register("worlds.command.link.remove") { children = listOf("worlds.command.link") }
        register("worlds.command.link.list") { children = listOf("worlds.command.link") }

        register("worlds.commands.save") {
            description = "Grants access to all world save commands"
            children = listOf(
                "worlds.command.save.save",
                "worlds.command.save.save-all",
                "worlds.command.save.save-off",
                "worlds.command.save.save-on"
            )
        }

        register("worlds.command.backup") { children = listOf("worlds.command") }
        register("worlds.command.clone") { children = listOf("worlds.command") }
        register("worlds.command.create") { children = listOf("worlds.command") }
        register("worlds.command.delete") { children = listOf("worlds.command") }
        register("worlds.command.import") { children = listOf("worlds.command") }
        register("worlds.command.info") { children = listOf("worlds.command") }
        register("worlds.command.link") { children = listOf("worlds.command") }
        register("worlds.command.list") { children = listOf("worlds.command") }
        register("worlds.command.load") { children = listOf("worlds.command") }
        register("worlds.command.save") { children = listOf("worlds.command") }
        register("worlds.command.save-all") { children = listOf("worlds.command") }
        register("worlds.command.save-off") { children = listOf("worlds.command") }
        register("worlds.command.save-on") { children = listOf("worlds.command") }
        register("worlds.command.seed") { children = listOf("minecraft.command.seed") }
        register("worlds.command.setspawn") { children = listOf("worlds.command") }
        register("worlds.command.spawn") { children = listOf("worlds.command") }
        register("worlds.command.teleport") { children = listOf("worlds.command") }
        register("worlds.command.unload") { children = listOf("worlds.command") }
    }
}

val versionString: String = project.version as String
val isRelease: Boolean = !versionString.contains("-pre")

val versions: List<String> = (property("worldsVersions") as String)
    .split(",")
    .map { it.trim() }

hangarPublish { // docs - https://docs.papermc.io/misc/hangar-publishing
    publications.register("plugin") {
        id.set("Worlds")
        version.set(versionString)
        changelog = System.getenv("CHANGELOG")
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
    changelog = System.getenv("CHANGELOG")
    versionType = if (isRelease) "release" else "beta"
    uploadFile.set(tasks.shadowJar)
    gameVersions.set(versions)
    syncBodyFrom.set(rootProject.file("README.md").readText())
    loaders.addAll((property("loaders") as String).split(",").map { it.trim() })
    dependencies {
        embedded.project("perworlds")
    }
}