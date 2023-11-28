package ru.cgstore.plugins

import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import ru.cgstore.routes.auth.auth
import ru.cgstore.routes.auth.test
import ru.cgstore.security.hash_service.HashingService
import ru.cgstore.security.token_service.TokenConfig
import ru.cgstore.security.token_service.TokenService
import ru.cgstore.storage.users.UsersService

fun Application.configureRouting(
    config: TokenConfig,
    usersService: UsersService,
    hashingService: HashingService,
    tokenService: TokenService
) {
    install(Resources)

    /*val usersService by inject<UsersService>()
    val hashingService by inject<HashingService>()
    val tokenService by inject<TokenService>()*/

    routing {
        auth(
            usersService = usersService,
            tokenService = tokenService,
            hashingService = hashingService,
            tokenConfig = config
        )
        test(usersService = usersService)
    }
}