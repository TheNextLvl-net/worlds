plugins {
    id("java")
    id("maven-publish")
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.26")
    compileOnly("net.thenextlvl.core:annotations:1.0.0")
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    implementation("net.thenextlvl.core:nbt:1.0.3")
    implementation("net.thenextlvl.core:api:3.1.13")

    annotationProcessor("org.projectlombok:lombok:1.18.26")
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
        repositories {
            maven {
                url = uri("https://repo.thenextlvl.net/releases")
                credentials {
                    if (extra.has("RELEASES_USER")) {
                        username = extra["RELEASES_USER"].toString()
                    }
                    if (extra.has("RELEASES_PASSWORD")) {
                        password = extra["RELEASES_PASSWORD"].toString()
                    }
                }
            }
        }
    }
}