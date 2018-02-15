package io.monkeypatch.talks.dephosting

import io.javalin.Context
import io.javalin.HaltException
import io.javalin.Javalin
import io.javalin.embeddedserver.Location.EXTERNAL
import io.javalin.event.EventType.*
import mu.KotlinLogging
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

private val logger = KotlinLogging.logger {}

fun serve(config: ServerConfig = ServerConfig(),
          proxies: DependenciesConfig = DependenciesConfig()): Javalin {
    val server = Javalin.create()
        // Log
        .requestLogLevel(config.logLevel)
        // Cors
        .enableCorsForAllOrigins()
        // Port
        .port(config.port)
        // Events
        .event(SERVER_STARTING) { logger.debug { "stating ..." } }
        .event(SERVER_STARTED) { logger.info { "started, available on port ${config.port}" } }
        .event(SERVER_START_FAILED) { logger.error { "fail to start, $it" } }
        .event(SERVER_STOPPING) { logger.info { "stopping ..." } }
        .event(SERVER_STOPPED) { logger.info { "stopped." } }
        // Static
        .enableStaticFiles("static")
        .enableStaticFiles(proxies.file.absolutePath, EXTERNAL) // static

    return proxies.proxies.toList()
        // proxies
        .fold(server) { srv, (key, proxy) ->
            val file = proxies.downloadFile(key)
            logger.info { "Proxy /$key/* to $file" }
            srv.before("/$key/*") { it.proxy(file, proxy) } // route
        }
        // Let's go
        .start()
}

private fun Context.proxy(dest: File, proxy: Proxy) {
//    val path1 = path().substring(1) // remove prefix '/'
    val path = splats().joinToString(separator = "/")
    val headers = headerMap()
        .filterKeys { it != "Accept-Encoding" }

    val proxyFile = dest.resolve(path)

    val (hosts, cache) = proxy

    if (proxyFile.exists()) {
        logger.debug { "File $proxyFile already present" }
        if (cache != null && Instant.now().toEpochMilli() - proxyFile.lastModified() > cache.toMillis()) {
            logger.debug { "Cache expired for $proxyFile" }
            downloadHosts(hosts, path, proxyFile, headers)
        }
    } else {
        logger.debug { "File $proxyFile not (yet) present" }
        downloadHosts(hosts, path, proxyFile, headers)
    }
}

private fun downloadHosts(hosts: List<String>,
                          path: String,
                          proxyFile: File,
                          headers: Map<String, String>) {
    val success = hosts.fold(false) { done, host ->
        done || tryDownload(host, path, proxyFile, headers)
    }
    if (!success) {
        logger.error { "💣 Cannot retrieve $path into $hosts" }
        throw HaltException(404, "Cannot retrieve $path into $hosts")
    }
}


private fun tryDownload(host: String, path: String, dest: File, headers: Map<String, String>): Boolean {
    val parentFile = dest.parentFile
    if (parentFile.mkdirs()) {
        logger.debug { "Create folder $parentFile" }
    }

    val url = URL(host + path)
    val connection = url.openHttpConnection(headers)

    logger.debug { "Try download $url ..." }
    return try {
        val copied = connection.inputStream.copyTo(dest.outputStream())
        logger.info { "✅ Download $url from $host" }
        logger.debug { "... to file $dest ($copied bytes)" }
        true
    } catch (_: IOException) {
        logger.warn { "fail to download $url" }
        false
    }
}

private fun URL.openHttpConnection(headers: Map<String, String> = emptyMap()): HttpURLConnection =
    (this.openConnection() as? HttpURLConnection
            ?: throw IllegalStateException("Only HTTP connection supported, got $this"))
        .apply {
            headers.forEach { key, value ->
                this.setRequestProperty(key, value)
            }
        }


