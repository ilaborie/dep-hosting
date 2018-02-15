package io.monkeypatch.talks.dephosting

import mu.KotlinLogging


private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {

    val serverConfig = ServerConfig.fromConfig()
    val dependenciesConfig = DependenciesConfig.fromConfig()

    logger.debug { "server configuration: $serverConfig" }
    logger.debug { "dependencies configuration: $dependenciesConfig" }

    serve(serverConfig, dependenciesConfig)
}