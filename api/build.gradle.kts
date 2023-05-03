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
    compileOnly("org.projectlombok:lombok:1.18.26")
    compileOnly("net.thenextlvl.core:annotations:1.0.0")
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    implementation("net.thenextlvl.core:api:3.1.10")

    annotationProcessor("org.projectlombok:lombok:1.18.26")
}