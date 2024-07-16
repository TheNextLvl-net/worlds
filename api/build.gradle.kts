plugins {
    id("java")
    id("maven-publish")
}

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

group = "net.thenextlvl.worlds"
version = "1.2.5"

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.thenextlvl.net/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.34")
    compileOnly("net.thenextlvl.core:annotations:2.0.1")
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")

    implementation("net.thenextlvl.core:nbt:1.4.0")
    implementation("net.thenextlvl.core:files:1.0.5-pre2")
    implementation("net.thenextlvl.core:adapters:1.0.9")

    annotationProcessor("org.projectlombok:lombok:1.18.34")
}


publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
    repositories.maven {
        val branch = if (version.toString().contains("-pre")) "snapshots" else "releases"
        url = uri("https://repo.thenextlvl.net/$branch")
        credentials {
            username = System.getenv("REPOSITORY_USER")
            password = System.getenv("REPOSITORY_TOKEN")
        }
    }
}