package ru.cgstore.routes.profile

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.*
import ru.cgstore.models.Failure
import ru.cgstore.models.PageResponse
import ru.cgstore.models.Response
import ru.cgstore.requests.profile.UpdateProfileRequest
import ru.cgstore.routes.profile.MESSAGES.SUCCESS_RECEIVE_MODELS
import ru.cgstore.routes.profile.MESSAGES.SUCCESS_RECEIVE_PROFILE_DATA
import ru.cgstore.routes.profile.MESSAGES.SUCCESS_UPDATE_PROFILE_DATA
import ru.cgstore.routes.profile.MESSAGES.USER_NOT_FOUND
import ru.cgstore.storage.render_model.RenderModelService
import ru.cgstore.storage.users.UsersService

private object MESSAGES {
    const val USER_NOT_FOUND = "Пользователь не найден"
    const val SUCCESS_RECEIVE_MODELS = "Модели успешно загружены"
    const val SUCCESS_RECEIVE_PROFILE_DATA = "Данные профиля успешно загружены"
    const val SUCCESS_UPDATE_PROFILE_DATA = "Данные профиля успешно обновлены"
}

fun Route.provideProfile(
    usersService: UsersService,
    renderModelService: RenderModelService,
) {
    authenticate {
        get<Profile> {
            val userID = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(HttpStatusCode.NotFound, USER_NOT_FOUND)
                return@get
            }

            usersService.read(userID).fold(
                ifLeft = { failure ->
                    when (failure) {
                        is Failure.ReadFailure -> {
                            call.respond(failure.statusCode, failure.message)
                            return@get
                        }

                        else -> {
                            call.respond(HttpStatusCode.BadRequest, failure.message)
                            return@get
                        }
                    }
                },
                ifRight = { profileDataResponse ->
                    call.respond(
                        HttpStatusCode.OK, Response(
                            message = SUCCESS_RECEIVE_PROFILE_DATA,
                            data = profileDataResponse
                        )
                    )
                    return@get
                }
            )
        }
        get<Profile.Models> { route ->
            val userID = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(HttpStatusCode.NotFound, USER_NOT_FOUND)
                return@get
            }

            renderModelService.readByUserID(
                author_id = userID,
                page = route.page,
                size = route.size
            ).fold(
                ifLeft = { failure ->
                    call.respond(HttpStatusCode.BadRequest, failure.message)
                    return@get
                },
                ifRight = { models ->
                    call.respond(
                        HttpStatusCode.OK,
                        PageResponse(
                            message = SUCCESS_RECEIVE_MODELS,
                            data = models,
                            size = route.size,
                            page = route.page,
                            number_of_found = models.size
                        )
                    )
                    return@get
                }
            )
        }
        post<Profile.Update> {
            val userID = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(HttpStatusCode.NotFound, USER_NOT_FOUND)
                return@post
            }

            val request = call.receive(UpdateProfileRequest::class)

            usersService.update(userID, request).fold(
                ifLeft = { failure ->
                    call.respond(HttpStatusCode.BadRequest, failure.message)
                    return@post
                },
                ifRight = {
                    call.respond(HttpStatusCode.OK, SUCCESS_UPDATE_PROFILE_DATA)
                    return@post
                }
            )
        }
    }
}