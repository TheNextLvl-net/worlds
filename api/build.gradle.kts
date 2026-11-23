dependencies {
    api("net.thenextlvl:static-binder:0.1.3")
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")

    testImplementation("io.papermc.paper:paper-api:26.1.2.build.+")
    testImplementation("net.thenextlvl:nbt:4.3.6")
}

tasks.withType<Javadoc>().configureEach {
    val options = options as StandardJavadocDocletOptions
    options.tags("apiNote:a:API Note:", "implSpec:a:Implementation Requirements:")
}