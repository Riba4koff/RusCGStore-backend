package ru.cgstore

import io.ktor.server.application.*
import ru.cgstore.plugins.*
import ru.cgstore.security.token_service.TokenConfig

fun main(args: Array<String>) {
    io.ktor.server.cio.EngineMain.main(args)
}

@Suppress("UNUSED")
fun Application.module() {
    val tokenConfig = TokenConfig(
        audience = environment.config.property("jwt.audience").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        expiresIn = 3L * 1000L * 60L * 60L * 24L,
        secret = System.getenv("secret")
    )

    configureResources()
    configureKoin()
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity(config = tokenConfig)
    configureRouting(config = tokenConfig)
}
