plugins {
    id("java")
}

group = "net.thenextlvl.worlds"

allprojects {
    group = rootProject.group
    version = rootProject.version

    apply { plugin("java") }
    apply { plugin("java-library") }

    java.disableAutoTargetJvm()

    java {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
    }

    tasks.compileJava {
        options.release.set(25)
    }

    repositories {
        mavenCentral()
        maven("https://repo.thenextlvl.net/releases")
        maven("https://repo.thenextlvl.net/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation(platform("org.junit:junit-bom:6.2.0-SNAPSHOT"))
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
}