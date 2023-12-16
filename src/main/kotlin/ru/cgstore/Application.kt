package ru.cgstore

import io.ktor.server.application.*
import io.ktor.server.resources.*
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
    val dataBaseConfig = DataBaseConfig(
        user = System.getenv("user"),
        password = System.getenv("password"),
        database = System.getenv("database"),
        ip = System.getenv("ip")
    )
    val tokenConfig = TokenConfig(
        audience = environment.config.property("jwt.audience").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        expiresIn = 3L * 1000L * 60L * 60L * 24L,
        secret = System.getenv("secret")
    )
    val database = configureDatabases(dataBaseConfig)
    val usersService = UsersServiceImpl(database)
    val tokenService = JwtTokenService()
    val hashingService = Sha256HashingService()

    //configureResources()
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity(config = tokenConfig)
    configureRouting(
        config = tokenConfig,
        usersService = usersService,
        hashingService = hashingService,
        tokenService = tokenService
    )
}
