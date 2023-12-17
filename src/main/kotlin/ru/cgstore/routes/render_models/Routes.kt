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
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_CREATE_MODEL
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_DELETE_MODEL
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_RECEIVE_MODEL
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_RECEIVE_MODELS
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_UPDATE_MODEL
import ru.cgstore.routes.render_models.MESSAGES.USER_NOT_FOUND
import ru.cgstore.storage.render_model.RenderModelService

private object MESSAGES {
    const val USER_NOT_FOUND = "Пользователь не найден"
    const val SUCCESS_RECEIVE_MODELS = "Модели успешно загружены"
    const val SUCCESS_RECEIVE_MODEL = "Модель успешно загружена"
    const val PARAMETER_ID_WAS_MISSING = "Пропущен параметр ID"
    const val CANNOT_DELETE_MODEL = "Вы не можете удалить модель, которая создана другим пользователем"
    const val SUCCESS_DELETE_MODEL = "Модель удалена"
    const val SUCCESS_CREATE_MODEL = "Модель создана"
    const val SUCCESS_UPDATE_MODEL = "Модель обновлена"
    const val MODEL_NOT_FOUND = "Модель не найдена"
}

fun Route.renderModels(
    renderModelService: RenderModelService,
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
                    message = PARAMETER_ID_WAS_MISSING,
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
            val userID: String? = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(
                    HttpStatusCode.NotFound, Response(
                        message = USER_NOT_FOUND,
                        data = null
                    )
                )
                return@put
            }

            val request = call.receive(CreateRenderModelRequest::class)

            renderModelService.create(
                author_id = userID,
                request = request
            ).fold(
                ifLeft = { failure ->
                    call.respond(
                        HttpStatusCode.BadRequest, Response(
                            message = failure.message,
                            data = null
                        )
                    )
                    return@put
                },
                ifRight = {
                    call.respond(
                        HttpStatusCode.OK, Response(
                            message = SUCCESS_CREATE_MODEL,
                            data = null
                        )
                    )
                    return@put
                }
            )
        }
        delete<Models.ID.Delete> { route ->
            val userID: String? = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Response(
                        message = USER_NOT_FOUND,
                        data = null
                    )
                )
                return@delete
            }

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

            renderModelService.readByID(route.parent.id).fold(
                ifLeft = { failure ->
                    when (failure) {
                        is Failure.ReadFailure -> call.respond(
                            status = failure.statusCode,
                            message = Response(
                                message = failure.message,
                                data = null
                            )
                        )

                        else -> call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = Response(
                                message = failure.message,
                                data = null
                            )
                        )
                    }
                    return@delete
                },
                ifRight = { model ->
                    if (model.author_id != userID) {
                        call.respond(
                            status = HttpStatusCode.Forbidden,
                            message = Response(
                                message = CANNOT_DELETE_MODEL,
                                data = null
                            )
                        )
                        return@delete
                    }
                    renderModelService.delete(route.parent.id).fold(
                        ifLeft = { failure ->
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = Response(
                                    message = failure.message,
                                    data = null
                                )
                            )
                        },
                        ifRight = {
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = Response(
                                    message = SUCCESS_DELETE_MODEL,
                                    data = null
                                )
                            )
                            return@delete
                        }
                    )
                }
            )
        }
        post<Models.ID.Update> { route ->
            val userID: String? = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = Response(
                        message = USER_NOT_FOUND,
                        data = null
                    )
                )
                return@post
            }

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

            val request = call.receive(UpdateModelRequest::class)

            renderModelService.update(
                id = route.parent.id,
                request = request
            ).fold(
                ifLeft = { failure ->
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = Response(
                            message = failure.message,
                            data = null
                        )
                    )
                    return@post
                },
                ifRight = {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = Response(
                            message = SUCCESS_UPDATE_MODEL,
                            data = null
                        )
                    )
                    return@post
                }
            )
        }
    }
}