package ru.cgstore.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.cgstore.security.hash_service.HashingService
import ru.cgstore.security.token_service.TokenConfig
import ru.cgstore.security.token_service.TokenService
import ru.cgstore.storage.users.UsersService
import io.ktor.server.resources.post
import org.koin.ktor.ext.inject
import ru.cgstore.requests.users.SignInUserRequest
import ru.cgstore.responses.auth.AuthResponse
import ru.cgstore.routes.auth.*
import ru.cgstore.security.hash_service.SaltedHash
import ru.cgstore.security.token_service.TokenClaim

fun Application.configureRouting(config: TokenConfig) {
    val usersService: UsersService by inject()
    val hashingService: HashingService by inject()
    val tokenService: TokenService by inject()

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