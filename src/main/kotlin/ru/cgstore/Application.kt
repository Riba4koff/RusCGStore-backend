package ru.cgstore

import io.ktor.server.application.*
import ru.cgstore.plugins.*
import ru.cgstore.security.token_service.TokenConfig
import ru.cgstore.storage.DataBaseConfig

fun main(args: Array<String>) {
    io.ktor.server.cio.EngineMain.main(args)
}

@Suppress("UNUSED")
fun Application.module() {
    val dataBaseConfig = DataBaseConfig(
        user = environment.config.property("postgres.user").getString(),
        password = environment.config.property("postgres.password").getString(),
        database = environment.config.property("postgres.database").getString(),
        ip = environment.config.property("postgres.ip").getString()
    )
    val tokenConfig = TokenConfig(
        audience = environment.config.property("jwt.audience").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        expiresIn = 3L * 1000L * 60L * 60L * 24L,
        secret = environment.config.property("jwt.secret").getString()
    )

    configureDatabases(dataBaseConfig)
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity(config = tokenConfig)
    configureRouting()
    koin()
}
