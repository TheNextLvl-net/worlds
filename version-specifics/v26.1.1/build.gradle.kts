java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

// tasks.compileJava { // todo: test on java 25 and java 21
//     options.release.set(25)
// }

dependencies {
    paperweight.paperDevBundle("26.1.1.build.20-alpha")
}