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
import ru.cgstore.routes.profile.MESSAGES.PARAMETER_LOGIN_WAS_NOT_FOUND_IN_TOKEN
import ru.cgstore.routes.profile.MESSAGES.SUCCESS_UPDATE_PROFILE_DATA
import ru.cgstore.routes.profile.MESSAGES.USER_NOT_FOUND
import ru.cgstore.storage.render_model.RenderModelService
import ru.cgstore.storage.users.UsersService

private object MESSAGES {
    const val USER_NOT_FOUND = "Пользователь не найден"
    const val SUCCESS_RECEIVE_MODELS = "Модели успешно загружены"
    const val SUCCESS_RECEIVE_PROFILE_DATA = "Данные профиля успешно загружены"
    const val SUCCESS_UPDATE_PROFILE_DATA = "Данные профиля успешно обновлены"
    const val PARAMETER_LOGIN_WAS_NOT_FOUND_IN_TOKEN = "Параметр логин не найден в JWT токене"
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

            usersService.read(userID).onLeft { failure ->
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
            }.onRight { profileDataResponse ->
                call.respond(
                    HttpStatusCode.OK, Response(
                        message = SUCCESS_RECEIVE_PROFILE_DATA,
                        data = profileDataResponse
                    )
                )
                return@get
            }
        }
        get<Profile.Models> { route ->
            val login = call.principal<JWTPrincipal>()?.get("login")

            if (login == null) {
                call.respond(HttpStatusCode.NotFound, PARAMETER_LOGIN_WAS_NOT_FOUND_IN_TOKEN)
                return@get
            }

            renderModelService.readByUserLogin(
                login = login,
                page = route.page,
                size = route.size
            ).onLeft { failure ->
                call.respond(HttpStatusCode.BadRequest, failure.message)
                return@get
            }.onRight { models ->
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
        }
        post<Profile.Update> {
            val userID = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(HttpStatusCode.NotFound, USER_NOT_FOUND)
                return@post
            }

            val request = call.receive(UpdateProfileRequest::class)

            usersService.update(userID, request).onLeft  { failure ->
                call.respond(HttpStatusCode.BadRequest, failure.message)
                return@post
            }.onRight {
                call.respond(HttpStatusCode.OK, SUCCESS_UPDATE_PROFILE_DATA)
                return@post
            }
        }
    }
}