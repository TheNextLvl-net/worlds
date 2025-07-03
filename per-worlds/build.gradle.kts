import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("idea")
    id("java")
    id("java-library")
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
version = "0.2.3"

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.21.6-R0.1-SNAPSHOT")

    api("org.bstats:bstats-bukkit:3.1.1-SNAPSHOT")

    api("net.thenextlvl.core:adapters:2.0.2")
    api("net.thenextlvl.core:i18n:3.2.0")
    api("net.thenextlvl.core:paper:2.2.1")

    api(project(":per-worlds-api"))

    testImplementation(platform("org.junit:junit-bom:6.0.0-SNAPSHOT"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

val generatedPath: java.nio.file.Path = layout.projectDirectory.dir("src/generated/java").asFile.toPath()
idea.module.generatedSourceDirs.add(generatedPath.toFile())
sourceSets.main {
    java.srcDir(generatedPath)
}

tasks.shadowJar {
    archiveBaseName.set("per-worlds")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    relocate("org.bstats", "net.thenextlvl.perworlds.bstats")
}

tasks.compileJava {
    dependsOn(project(":source-generator").tasks.named("generateSources"))
}

paper {
    name = "PerWorlds"
    main = "net.thenextlvl.perworlds.PerWorldsPlugin"
    apiVersion = "1.21.5"
    description = "Per-world customization for gameplay and settings"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    website = "https://thenextlvl.net"
    authors = listOf("NonSwag")
    // foliaSupported = true // way too many events still not being called on folia
    permissions {
        register("perworlds.command.group") { children = listOf("perworlds.command") }
        register("perworlds.command.group.add") { children = listOf("perworlds.command.group") }
        register("perworlds.command.group.create") { children = listOf("perworlds.command.group") }
        register("perworlds.command.group.delete") { children = listOf("perworlds.command.group") }
        register("perworlds.command.group.list") { children = listOf("perworlds.command.group") }
        register("perworlds.command.group.option") { children = listOf("perworlds.command.group") }
        register("perworlds.command.group.remove") { children = listOf("perworlds.command.group") }
        register("perworlds.command.group.spawn") { children = listOf("perworlds.command.group") }
        register("perworlds.command.group.spawn.set") { children = listOf("perworlds.command.group.spawn") }
        register("perworlds.command.group.spawn.unset") { children = listOf("perworlds.command.group.spawn") }
        register("perworlds.command.group.teleport") { children = listOf("perworlds.command.group") }
    }
}

val versionString: String = project.version as String
val isRelease: Boolean = !versionString.contains("-pre")

val versions: List<String> = (property("perWorldsVersions") as String)
    .split(",")
    .map { it.trim() }

hangarPublish { // docs - https://docs.papermc.io/misc/hangar-publishing
    publications.register("per-worlds") {
        id.set("PerWorlds")
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
    projectId.set("lpfQmSV2")
    changelog = System.getenv("CHANGELOG")
    versionType = if (isRelease) "release" else "beta"
    uploadFile.set(tasks.shadowJar)
    gameVersions.set(versions)
    loaders.add("paper")
    dependencies {
        incompatible.project("worlds")
    }
}
