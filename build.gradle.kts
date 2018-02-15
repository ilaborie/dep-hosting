plugins {
    application
    kotlin("jvm") version "1.2.21"
}

application {
    mainClassName = "io.monkeypatch.talks.dephosting.MainKt"
}

dependencies {
    val config4kVersion = "0.3.0"
    val javalinVersion = "1.3.0"
    val kotlinLoggingVersion = "1.4.9"

    compile(kotlin("stdlib"))
    compile(kotlin("reflect"))
    compile("io.javalin", "javalin", javalinVersion)
    compile("io.github.config4k", "config4k", config4kVersion)
    compile("io.github.microutils", "kotlin-logging", kotlinLoggingVersion)
}

repositories {
    jcenter()
}
