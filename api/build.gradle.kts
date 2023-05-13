plugins {
    id("java")
    id("maven-publish")
}

group = "net.thenextlvl.worlds"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.26")
    compileOnly("net.thenextlvl.core:annotations:1.0.0")
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    implementation("net.thenextlvl.core:api:3.1.12")

    annotationProcessor("org.projectlombok:lombok:1.18.26")
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
                    username = extra["RELEASES_USER"].toString()
                    password = extra["RELEASES_PASSWORD"].toString()
                }
            }
        }
    }
}