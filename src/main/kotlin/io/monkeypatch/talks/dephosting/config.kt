package io.monkeypatch.talks.dephosting

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.javalin.LogLevel
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant


private val config = ConfigFactory.load()
private val logger = KotlinLogging.logger {}

data class ServerConfig(val port: Int = 8080, private val log: String = LogLevel.STANDARD.name) {

    init {
        if (port == 0) {
            logger.info { "using a random port" }
        }
        if (port < 0) {
            logger.error { "port $port should be >= 0" }
        }
        logger.info { "Using server log level: ${logLevel.name}" }
    }

    companion object {
        fun fromConfig(): ServerConfig =
            config.extract("server")
    }

    val logLevel: LogLevel
        get() = LogLevel.valueOf(log)
}

data class Proxy(val hosts: List<String> = emptyList(), val cache: Duration? = null, val npmProxy: Boolean = false) {
    init {
        if (hosts.isEmpty()) {
            logger.warn { "No hosts to proxy !" }
        }
    }

    fun isOutdated(proxyFile: File): Boolean {
        val delta by lazy { Instant.now().toEpochMilli() - proxyFile.lastModified() }
        return cache != null && delta > cache.toMillis()
    }
}

data class DependenciesConfig(private val downloadPath: String? = null,
                              val proxies: Map<String, Proxy> = emptyMap()) {

    init {
        proxies.forEach { key, proxy ->
            logger.debug { "Proxy for $key: $proxy" }
        }
    }

    val file: File by lazy {
        if (downloadPath != null) {
            Paths.get(downloadPath)
                .toFile()
                .apply {
                    if (this.mkdirs())
                        logger.info { "Create $this as repository directory" }
                    else
                        logger.debug { "Using $this as repository directory" }

                    if (!isDirectory) {
                        logger.error { "$this expected to be a a directory" }
                    }
                }
        } else {
            Files.createTempDirectory("dep-hosting")
                .toFile()
                .apply {
                    logger.info { "Using a temporary directory $this as repository directory" }
                }
        }
    }

    fun downloadFile(key: String): File =
        file.resolve(key)
            .apply {
                this.mkdirs()
            }

    companion object {
        fun fromConfig(): DependenciesConfig =
            config.extract("dependencies")
    }
}


