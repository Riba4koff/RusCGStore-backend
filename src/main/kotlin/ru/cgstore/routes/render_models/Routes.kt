package ru.cgstore.routes.render_models

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.resources.get
import io.ktor.server.resources.put
import io.ktor.server.resources.post
import io.ktor.server.resources.delete
import io.ktor.server.response.*
import ru.cgstore.models.Failure
import ru.cgstore.models.PageResponse
import ru.cgstore.models.Response
import ru.cgstore.requests.render_models.CreateRenderModelRequest
import ru.cgstore.requests.render_models.UpdateModelRequest
import ru.cgstore.routes.render_models.MESSAGES.CANNOT_DELETE_MODEL
import ru.cgstore.routes.render_models.MESSAGES.MODEL_NOT_FOUND
import ru.cgstore.routes.render_models.MESSAGES.PARAMETER_ID_WAS_MISSING
import ru.cgstore.routes.render_models.MESSAGES.PARAMETER_LOGIN_WAS_NOT_FOUND_IN_JWT_TOKEN
import ru.cgstore.routes.render_models.MESSAGES.PARAMETER_LOGIN_WAS_MISSING
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_CREATE_MODEL
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_DELETE_MODEL
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_RECEIVE_MODEL
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_RECEIVE_MODELS
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_UPDATE_MODEL
import ru.cgstore.storage.render_model.RenderModelService
import ru.cgstore.storage.users.UsersService

private object MESSAGES {
    const val USER_NOT_FOUND = "Пользователь не найден"
    const val SUCCESS_RECEIVE_MODELS = "Модели успешно загружены"
    const val SUCCESS_RECEIVE_MODEL = "Модель успешно загружена"
    const val PARAMETER_LOGIN_WAS_NOT_FOUND_IN_JWT_TOKEN = "Параметр ID не найден в JWT токене"
    const val PARAMETER_LOGIN_WAS_MISSING = "Пропущен параметр login"
    const val PARAMETER_ID_WAS_MISSING = "Пропущен параметр ID"
    const val CANNOT_DELETE_MODEL = "Вы не можете удалить модель, которая создана другим пользователем"
    const val SUCCESS_DELETE_MODEL = "Модель удалена"
    const val SUCCESS_CREATE_MODEL = "Модель создана"
    const val SUCCESS_UPDATE_MODEL = "Модель обновлена"
    const val MODEL_NOT_FOUND = "Модель не найдена"
}

fun Route.renderModels(
    renderModelService: RenderModelService,
    userService: UsersService,
) {
    get<Models.All> { route ->
        renderModelService.readAll(page = route.page, size = route.size).fold(
            ifLeft = { failure ->
                call.respond(
                    HttpStatusCode.BadRequest, Response(
                        message = failure.message,
                        data = null
                    )
                )
                return@get
            },
            ifRight = { models ->
                call.respond(
                    HttpStatusCode.OK, PageResponse(
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
    get<Models.ID> { route ->
        if (route.id == null) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = Response(
                    message = PARAMETER_LOGIN_WAS_NOT_FOUND_IN_JWT_TOKEN,
                    data = null
                )
            )
            return@get
        }

        renderModelService.readByID(id = route.id).fold(
            ifLeft = { failure ->
                val status = if (failure is Failure.ReadFailure) HttpStatusCode.NotFound
                else HttpStatusCode.BadRequest
                call.respond(
                    status = status,
                    message = Response(
                        message = MODEL_NOT_FOUND,
                        data = null
                    )
                )
                return@get
            },
            ifRight = { model ->
                call.respond(
                    status = HttpStatusCode.OK,
                    message = Response(
                        message = SUCCESS_RECEIVE_MODEL,
                        data = model
                    )
                )
                return@get
            }
        )
    }
    authenticate {
        put<Models.Create> {
            val login = call.principal<JWTPrincipal>()?.get("login")

            if (login == null) {
                call.respond(HttpStatusCode.NotFound, PARAMETER_LOGIN_WAS_NOT_FOUND_IN_JWT_TOKEN)
                return@put
            }

            userService.readByLogin(login).onLeft { failure ->
                call.respond(
                    status = HttpStatusCode.NotFound, message = Response(
                        message = failure.message,
                        data = null
                    )
                )
                return@put
            }.onRight { user ->
                val request = call.receive(CreateRenderModelRequest::class)

                renderModelService.create(
                    login = user.login,
                    request = request
                ).onLeft { failure ->
                    call.respond(
                        HttpStatusCode.BadRequest, Response(
                            message = failure.message,
                            data = null
                        )
                    )
                    return@put
                }.onRight {
                    call.respond(
                        HttpStatusCode.OK, Response(
                            message = SUCCESS_CREATE_MODEL,
                            data = null
                        )
                    )
                    return@put
                }
            }
        }
        delete<Models.ID.Delete> { route ->
            val login = call.principal<JWTPrincipal>()?.get("login")

            if (login == null) {
                call.respond(HttpStatusCode.NotFound, PARAMETER_LOGIN_WAS_NOT_FOUND_IN_JWT_TOKEN)
                return@delete
            }

            // If parameter id was missing
            if (route.parent.id == null) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Response(
                        message = PARAMETER_ID_WAS_MISSING,
                        data = null
                    )
                )
            }

            userService.readByLogin(login).onLeft { failure ->
                // If user not found
                call.respond(
                    status = HttpStatusCode.NotFound, message = Response(
                        message = failure.message,
                        data = null
                    )
                )
                return@delete
            }.onRight { user ->
                // If parameter id was missing
                if (route.parent.id == null) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = Response(
                            message = PARAMETER_ID_WAS_MISSING,
                            data = null
                        )
                    )
                    return@delete
                }

                // Read model by id
                renderModelService.readByID(route.parent.id).onLeft { failure ->
                    when (failure) {
                        // Model not found
                        is Failure.ReadFailure -> call.respond(
                            status = failure.statusCode,
                            message = Response(
                                message = failure.message,
                                data = null
                            )
                        )

                        // Some error
                        else -> call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = Response(
                                message = failure.message,
                                data = null
                            )
                        )
                    }
                    return@delete
                }.onRight { model ->
                    // If user is author of model
                    if (model.author_login != user.login) {
                        call.respond(
                            status = HttpStatusCode.Forbidden,
                            message = Response(
                                message = CANNOT_DELETE_MODEL,
                                data = null
                            )
                        )
                        return@delete
                    }

                    // delete model
                    renderModelService.delete(route.parent.id).onLeft { failure ->
                        // Some error
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = Response(
                                message = failure.message,
                                data = null
                            )
                        )
                    }.onRight {

                        // Success
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = Response(
                                message = SUCCESS_DELETE_MODEL,
                                data = null
                            )
                        )
                        return@delete
                    }
                }
            }
        }
        post<Models.ID.Update> { route ->
            val login = call.principal<JWTPrincipal>()?.get("login")

            if (login == null) {
                call.respond(HttpStatusCode.NotFound, PARAMETER_LOGIN_WAS_NOT_FOUND_IN_JWT_TOKEN)
                return@post
            }

            userService.readByLogin(login).onLeft { failure ->
                // If user not found
                call.respond(
                    status = HttpStatusCode.NotFound, message = Response(
                        message = failure.message,
                        data = null
                    )
                )
                return@post
            }.onRight { user ->
                val request = call.receive(UpdateModelRequest::class)
                // If parameter id was missing
                if (route.parent.id == null) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = Response(
                            message = PARAMETER_ID_WAS_MISSING,
                            data = null
                        )
                    )
                    return@post
                }
                // Find the model
                renderModelService.readByID(route.parent.id).onLeft { failure ->
                    when (failure) {
                        // Model not found
                        is Failure.ReadFailure -> call.respond(
                            status = failure.statusCode,
                            message = Response(
                                message = failure.message,
                                data = null
                            )
                        )
                        // Some error
                        else -> call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = Response(
                                message = failure.message,
                                data = null
                            )
                        )
                    }
                    return@post
                }.onRight { model ->
                    // If user is not an author of model
                    if (model.author_login != user.login) {
                        call.respond(
                            status = HttpStatusCode.Forbidden,
                            message = Response(
                                message = CANNOT_DELETE_MODEL,
                                data = null
                            )
                        )
                        return@post
                    }
                    // Updating model
                    renderModelService.update(
                        id = route.parent.id,
                        request = request
                    ).onLeft { failure ->
                        // Some error
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = Response(
                                message = failure.message,
                                data = null
                            )
                        )
                        return@post
                    }.onRight {
                        // Model was updated
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = Response(
                                message = SUCCESS_UPDATE_MODEL,
                                data = null
                            )
                        )
                        return@post
                    }
                }
            }
        }
    }
}