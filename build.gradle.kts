plugins {
    application
    kotlin("jvm") version "1.2.21"
}

val main = "io.monkeypatch.talks.dephosting.MainKt"
application {
    mainClassName = main
}

dependencies {
    val config4kVersion = "0.3.2"
    val javalinVersion = "1.3.0"
    val kotlinLoggingVersion = "1.4.9"

    compile(kotlin("stdlib"))
    compile(kotlin("reflect"))

    compile("io.javalin", "javalin", javalinVersion)
    compile("io.github.config4k", "config4k", config4kVersion)
    compile("io.github.microutils", "kotlin-logging", kotlinLoggingVersion)
    runtime("ch.qos.logback", "logback-classic", "1.2.3")
}

repositories {
    jcenter()
}


val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fatjar"
    manifest {
        attributes["Main-Class"] = main
    }
    from(configurations.runtime.map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks["jar"] as CopySpec)
}

tasks {
    "assemble" {
        dependsOn(fatJar)
    }
}
