package ru.cgstore

import io.ktor.server.application.*
import io.ktor.server.resources.*
import org.koin.core.context.startKoin
import org.koin.dsl.ModuleDeclaration
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import ru.cgstore.di.appModule
import ru.cgstore.plugins.*
import ru.cgstore.security.hash_service.Sha256HashingService
import ru.cgstore.security.token_service.JwtTokenService
import ru.cgstore.security.token_service.TokenConfig
import ru.cgstore.storage.DataBaseConfig
import ru.cgstore.storage.users.UsersServiceImpl

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
