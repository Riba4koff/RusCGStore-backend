package ru.cgstore.routes.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.cgstore.requests.users.SignInUserRequest
import io.ktor.server.resources.post
import org.koin.ktor.ext.inject
import ru.cgstore.requests.users.SignUpUserRequest
import ru.cgstore.responses.auth.AuthResponse
import ru.cgstore.security.hash_service.HashingService
import ru.cgstore.security.hash_service.SaltedHash
import ru.cgstore.security.token_service.TokenClaim
import ru.cgstore.security.token_service.TokenConfig
import ru.cgstore.security.token_service.TokenService
import ru.cgstore.storage.users.UsersService
import ru.cgstore.storage.users.UsersServiceImpl

fun Route.auth(
    usersService: UsersService,
    tokenService: TokenService,
    hashingService: HashingService,
    tokenConfig: TokenConfig
) {
    signIn(
        usersService = usersService,
        tokenService = tokenService,
        hashingService = hashingService,
        tokenConfig = tokenConfig
    )
    signUp(
        usersService = usersService,
        hashingService = hashingService
    )
}

private fun Route.signIn(
    usersService: UsersService,
    tokenService: TokenService,
    hashingService: HashingService,
    tokenConfig: TokenConfig
) {
    post<Auth.SignIn> { _ ->
        val request = call.receive(SignInUserRequest::class)
        usersService.readByLogin(request.login).fold(
            ifLeft = { failure ->
                call.respond(HttpStatusCode.NotFound, failure.message)
                return@post
            },
            ifRight = { user ->
                val isValidPassword = hashingService.verify(request.password, SaltedHash(user.hash, user.salt))

                if (isValidPassword.not()) call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = "Пароль неверный"
                )

                val token = tokenService.generate(
                    config = tokenConfig,
                    TokenClaim(name = "id", value = user.id),
                )

                call.respond(HttpStatusCode.OK, AuthResponse(token))
                return@post
            }
        )
    }
}

private fun Route.signUp(
    usersService: UsersService,
    hashingService: HashingService
) {
    post<Auth.SignUp> { _ ->
        val request = call.receive(SignUpUserRequest::class)

        val areFieldsBlank =
            request.email.isBlank()
                    || request.login.isBlank()
                    || request.password.isBlank()

        val isPwdTooShort = request.password.length < 8

        if (areFieldsBlank || isPwdTooShort) {
            call.respond(HttpStatusCode.BadRequest, "Fields is empty or length password < 8")
            return@post
        }

        usersService.phoneExists(request.phone).fold(
            ifLeft = { failure ->
                call.respond(HttpStatusCode.Conflict, failure.message)
                return@post
            },
            ifRight = { return@fold }
        )

        usersService.loginExists(request.login).fold(
            ifLeft = { failure ->
                call.respond(HttpStatusCode.Conflict, failure.message)
                return@post
            },
            ifRight = { return@fold }
        )

        usersService.emailExists(request.email).fold(
            ifLeft = { failure ->
                call.respond(HttpStatusCode.Conflict, failure.message)
                return@post
            },
            ifRight = { return@fold }
        )

        val saltedHash = hashingService.generateSaltedHash(value = request.password)

        usersService.create(
            request = request,
            saltedHash = saltedHash
        )

        call.respond(status = HttpStatusCode.OK, "")
        return@post
    }
}