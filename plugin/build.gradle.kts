import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("com.gradleup.shadow") version "9.4.2"
    id("com.modrinth.minotaur") version "2.+"
    id("de.eldoria.plugin-yml.paper") version "0.9.0"
    id("io.papermc.hangar-publish-plugin") version "0.1.4"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    testImplementation("io.papermc.paper:paper-api:26.1.2.build.+")
    implementation(project(":api"))

    implementation(project(":version-specifics"))
    implementation(project(":version-specifics:v26.1.2"))

    implementation("net.thenextlvl.version-checker:modrinth-paper:1.0.1")
    implementation("net.thenextlvl:i18n:1.2.0")
    implementation("net.thenextlvl:nbt:4.3.6")

    implementation("dev.faststats.metrics:bukkit:0.26.1")
    implementation("org.bstats:bstats-bukkit:3.2.1")
}

tasks.shadowJar {
    archiveBaseName.set("worlds")
    relocate("org.bstats", "net.thenextlvl.worlds.metrics.bstats")
    relocate("dev.faststats", "net.thenextlvl.worlds.metrics.faststats")
}

paper {
    name = "Worlds"
    main = "net.thenextlvl.worlds.WorldsPlugin"
    apiVersion = "26.1"
    description = "Create, delete and manage your worlds"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    website = "https://thenextlvl.net/docs/worlds"
    authors = listOf("NonSwag")
    foliaSupported = true

    permissions {
        register("worlds.admin") {
            description = "Allows access to all world commands"
            children = listOf(
                "worlds.command.backup",
                "worlds.command.clone",
                "worlds.command.create",
                "worlds.command.delete",
                "worlds.command.import",
                "worlds.command.info",
                "worlds.command.list",
                "worlds.command.load",
                "worlds.command.recreate",
                "worlds.command.regenerate",
                "worlds.command.save",
                "worlds.command.save-all",
                "worlds.command.save-off",
                "worlds.command.save-on",
                "worlds.command.setspawn",
                "worlds.command.spawn",
                "worlds.command.teleport",
                "worlds.command.unload"
            )
        }

        register("worlds.commands.save") {
            description = "Grants access to all world save commands"
            children = listOf(
                "worlds.command.save",
                "worlds.command.save-all",
                "worlds.command.save-off",
                "worlds.command.save-on"
            )
        }

        register("worlds.commands.backup") {
            description = "Grants access to all world backup commands"
            children = listOf(
                "worlds.command.backup"
            )
        }

        register("worlds.command")
        register("worlds.command.backup") { children = listOf("worlds.command") }
        register("worlds.command.backup.create") { children = listOf("worlds.command.backup") }
        register("worlds.command.backup.delete") { children = listOf("worlds.command.backup") }
        register("worlds.command.backup.list") { children = listOf("worlds.command.backup") }
        register("worlds.command.backup.prune") { children = listOf("worlds.command.backup") }
        register("worlds.command.backup.restore") { children = listOf("worlds.command.backup") }
        register("worlds.command.clone") { children = listOf("worlds.command") }
        register("worlds.command.create") { children = listOf("worlds.command") }
        register("worlds.command.delete") { children = listOf("worlds.command") }
        register("worlds.command.import") { children = listOf("worlds.command") }
        register("worlds.command.info") { children = listOf("worlds.command") }
        register("worlds.command.list") { children = listOf("worlds.command") }
        register("worlds.command.load") { children = listOf("worlds.command") }
        register("worlds.command.recreate") { children = listOf("worlds.command") }
        register("worlds.command.regenerate") { children = listOf("worlds.command") }
        register("worlds.command.save") { children = listOf("worlds.command") }
        register("worlds.command.save-all") { children = listOf("worlds.command", "minecraft.command.save-all") }
        register("worlds.command.save-off") { children = listOf("worlds.command", "minecraft.command.save-off") }
        register("worlds.command.save-on") { children = listOf("worlds.command", "minecraft.command.save-on") }
        register("worlds.command.setspawn") { children = listOf("worlds.command", "minecraft.command.setworldspawn") }
        register("worlds.command.spawn") { children = listOf("worlds.command") }
        register("worlds.command.teleport") { children = listOf("worlds.command") }
        register("worlds.command.unload") { children = listOf("worlds.command") }
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
}
