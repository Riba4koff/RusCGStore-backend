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
import ru.cgstore.requests.render_models.CreateRenderModelRequest
import ru.cgstore.requests.render_models.UpdateModelRequest
import ru.cgstore.routes.render_models.MESSAGES.CANNOT_DELETE_MODEL
import ru.cgstore.routes.render_models.MESSAGES.PARAMETER_ID_WAS_MISSING
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_CREATE_MODEL
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_DELETE_MODEL
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_RECEIVE_MODELS
import ru.cgstore.routes.render_models.MESSAGES.SUCCESS_UPDATE_MODEL
import ru.cgstore.routes.render_models.MESSAGES.USER_NOT_FOUND
import ru.cgstore.storage.render_model.RenderModelService

private object MESSAGES {
    const val USER_NOT_FOUND = "Пользователь не найден"
    const val SUCCESS_RECEIVE_MODELS = "Модели успешно загружены"
    const val PARAMETER_ID_WAS_MISSING = "Пропущен параметр ID"
    const val CANNOT_DELETE_MODEL = "Вы не можете удалить модель, которая создана другим пользователем"
    const val SUCCESS_DELETE_MODEL = "Модель удалена"
    const val SUCCESS_CREATE_MODEL = "Модель создана"
    const val SUCCESS_UPDATE_MODEL = "Модель обновлена"
}

fun Route.renderModels(
    renderModelService: RenderModelService,
) {
    authenticate {
        put<Models.Create> {
            val userID: String? = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(HttpStatusCode.NotFound, USER_NOT_FOUND)
                return@put
            }

            val request = call.receive(CreateRenderModelRequest::class)

            renderModelService.create(
                author_id = userID,
                request = request
            ).fold(
                ifLeft = { failure ->
                    call.respond(HttpStatusCode.BadRequest, failure.message)
                    return@put
                },
                ifRight = {
                    call.respond(HttpStatusCode.OK, SUCCESS_CREATE_MODEL)
                    return@put
                }
            )
        }
        get<Models.All> { route ->
            renderModelService.readAll(page = route.page, size = route.size).fold(
                ifLeft = { failure ->
                    call.respond(HttpStatusCode.BadRequest, failure.message)
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
        delete<Models.ID.Delete> { route ->
            val userID: String? = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(HttpStatusCode.NotFound, USER_NOT_FOUND)
                return@delete
            }

            if (route.parent.id == null) {
                call.respond(HttpStatusCode.BadRequest, PARAMETER_ID_WAS_MISSING)
                return@delete
            }

            renderModelService.readByID(route.parent.id).fold(
                ifLeft = { failure ->
                    when (failure) {
                        is Failure.ReadFailure -> call.respond(failure.statusCode, failure.message)
                        else -> call.respond(HttpStatusCode.BadRequest, failure.message)
                    }
                    return@delete
                },
                ifRight = { model ->
                    if (model.author_id != userID) {
                        call.respond(HttpStatusCode.Forbidden, CANNOT_DELETE_MODEL)
                        return@delete
                    }
                    renderModelService.delete(route.parent.id).fold(
                        ifLeft = { failure ->
                            call.respond(HttpStatusCode.BadRequest, failure.message)
                        },
                        ifRight = {
                            call.respond(HttpStatusCode.OK, SUCCESS_DELETE_MODEL)
                            return@delete
                        }
                    )
                }
            )
        }
        post<Models.ID.Update> { route ->
            val userID: String? = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(HttpStatusCode.NotFound, USER_NOT_FOUND)
                return@post
            }

            if (route.parent.id == null) {
                call.respond(HttpStatusCode.BadRequest, PARAMETER_ID_WAS_MISSING)
                return@post
            }

            val request = call.receive(UpdateModelRequest::class)

            renderModelService.update(
                id = route.parent.id,
                request = request
            ).fold(
                ifLeft = { failure ->
                    call.respond(HttpStatusCode.BadRequest, failure.message)
                    return@post
                },
                ifRight = {
                    call.respond(HttpStatusCode.OK, SUCCESS_UPDATE_MODEL)
                    return@post
                }
            )
        }
    }
}