package ru.cgstore.routes.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.cgstore.storage.users.UsersService

fun Route.test(usersService: UsersService) {
    authenticate {
        post("test") {
            val id = call.principal<JWTPrincipal>()?.get("id")!!

            usersService.read(id).fold(
                ifLeft = { failure ->
                    call.respond(HttpStatusCode.NotFound, failure.message)
                },
                ifRight = { user ->
                    call.respond(HttpStatusCode.OK, "Hello, ${user.login}")
                    return@post
                }
            )
        }
    }
}