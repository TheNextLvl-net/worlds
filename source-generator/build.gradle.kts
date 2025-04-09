plugins {
    id("java")
}

group = "net.thenextlvl.worlds"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    implementation("com.palantir.javapoet:javapoet:0.6.0")
    implementation(project(":per-worlds-api"))

    testImplementation(platform("org.junit:junit-bom:5.13.0-M2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.register<JavaExec>("generate") {
    mainClass.set("net.thenextlvl.worlds.generator.SourceGenerator")
    classpath(sourceSets.main.map { it.runtimeClasspath })
    args(rootProject.layout.projectDirectory.dir("per-worlds/src/generated/java").asFile.absolutePath)
}

tasks.test {
    useJUnitPlatform()
}