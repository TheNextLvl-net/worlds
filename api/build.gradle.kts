plugins {
    id("maven-publish")
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    api("net.thenextlvl:static-binder:0.1.3")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation("net.thenextlvl:nbt:4.3.4")
}

tasks.withType<Javadoc>().configureEach {
    val options = options as StandardJavadocDocletOptions
    options.tags("apiNote:a:API Note:", "implSpec:a:Implementation Requirements:")
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = "worlds"
        groupId = "net.thenextlvl"
        pom.url.set("https://thenextlvl.net/docs/worlds")
        pom.scm {
            val repository = "TheNextLvl-net/worlds"
            url.set("https://github.com/$repository")
            connection.set("scm:git:git://github.com/$repository.git")
            developerConnection.set("scm:git:ssh://github.com/$repository.git")
        }
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