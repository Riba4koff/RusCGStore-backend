package ru.cgstore.routes.feedback

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ru.cgstore.storage.feedback.FeedBackService
import io.ktor.server.resources.get
import io.ktor.server.resources.put
import io.ktor.server.resources.delete
import io.ktor.server.response.*
import ru.cgstore.models.Failure
import ru.cgstore.models.PageResponse
import ru.cgstore.models.Response
import ru.cgstore.models.users.UserRole
import ru.cgstore.requests.feedback.CreateFeedBackRequest
import ru.cgstore.requests.render_models.CreateRenderModelRequest
import ru.cgstore.routes.feedback.MESSAGES.FEEDBACK_NOT_FOUND
import ru.cgstore.routes.feedback.MESSAGES.NOT_ENOUGH_RIGHTS
import ru.cgstore.routes.feedback.MESSAGES.SUCCESS_CREATED_FEEDBACK
import ru.cgstore.routes.feedback.MESSAGES.SUCCESS_RECEIVE_FEEDBACK
import ru.cgstore.routes.feedback.MESSAGES.SUCCESS_RECEIVE_FEEDBACKS
import ru.cgstore.routes.feedback.MESSAGES.SUCCESS_REMOVED_FEEDBACK
import ru.cgstore.routes.feedback.MESSAGES.USER_NOT_FOUND
import ru.cgstore.storage.render_model.RenderModelService
import ru.cgstore.storage.users.UsersService
import kotlin.math.cos

private object MESSAGES {
    const val USER_NOT_FOUND = "Пользователь не найден"
    const val FEEDBACK_NOT_FOUND = "Отзыв не найден"
    const val SUCCESS_RECEIVE_FEEDBACKS = "Отзывы успешно загружены"
    const val SUCCESS_RECEIVE_FEEDBACK = "Отзыв успешно загружен"
    const val NOT_ENOUGH_RIGHTS = "Недостаточно прав"
    const val SUCCESS_REMOVED_FEEDBACK = "Отзыв удален"
    const val SUCCESS_CREATED_FEEDBACK = "Отзыв успешно добавлен"
}

fun Route.feedback(
    feedBackService: FeedBackService,
    usersService: UsersService,
    renderModelService: RenderModelService
) {
    authenticate {
        get<FeedBack.All> { route ->
            val userID: String? = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(HttpStatusCode.NotFound, USER_NOT_FOUND)
                return@get
            }

            feedBackService.getAllByModelID(
                model_id = route.modelID,
                size = route.size,
                page = route.page
            ).fold(
                ifLeft = { failure ->
                    call.respond(HttpStatusCode.BadRequest, failure.message)
                    return@get
                },
                ifRight = { feedbacks ->
                    call.respond(
                        HttpStatusCode.OK, PageResponse(
                            message = SUCCESS_RECEIVE_FEEDBACKS,
                            data = feedbacks,
                            size = route.size,
                            page = route.page,
                            number_of_found = feedbacks.size
                        )
                    )
                    return@get
                }
            )
        }
        get<FeedBack.ID> { route ->
            val userID: String? = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(HttpStatusCode.NotFound, USER_NOT_FOUND)
                return@get
            }

            feedBackService.getFeedBackByID(route.id).fold(
                ifLeft = { failure ->
                    call.respond(HttpStatusCode.BadRequest, failure.message)
                },
                ifRight = { feedback ->
                    if (feedback == null) call.respond(HttpStatusCode.NotFound, FEEDBACK_NOT_FOUND) else
                        call.respond(HttpStatusCode.OK, Response(message = SUCCESS_RECEIVE_FEEDBACK, data = feedback))
                    return@get
                }
            )
        }
        delete<FeedBack.ID> { route ->
            val userID: String? = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(HttpStatusCode.NotFound, USER_NOT_FOUND)
                return@delete
            }

            usersService.read(userID).fold(
                ifLeft = { failure ->
                    call.respond(HttpStatusCode.BadRequest, failure.message)
                    return@delete
                },
                ifRight = { user ->
                    if (user.role != UserRole.ADMIN) call.respond(HttpStatusCode.Forbidden, NOT_ENOUGH_RIGHTS)
                    else feedBackService.delete(route.id).fold(
                        ifLeft = { failure ->
                            call.respond(HttpStatusCode.BadRequest, failure.message)
                            return@delete
                        },
                        ifRight = {
                            call.respond(HttpStatusCode.OK, SUCCESS_REMOVED_FEEDBACK)
                        }
                    )
                }
            )


            call.respond(HttpStatusCode.OK, "hello, world!")
        }
        put<FeedBack.Create> { _ ->
            val userID: String? = call.principal<JWTPrincipal>()?.get("id")

            if (userID == null) {
                call.respond(HttpStatusCode.NotFound, USER_NOT_FOUND)
                return@put
            }

            val request = call.receive(CreateFeedBackRequest::class)

            renderModelService.readByID(request.model_id).fold(
                ifLeft = { failure ->
                    when (failure) {
                        is Failure.ReadFailure -> call.respond(failure.statusCode, failure.message)
                        else -> call.respond(HttpStatusCode.BadRequest, failure.message)
                    }
                    return@put
                },
                ifRight = { model ->
                    feedBackService.create(
                        modelID = model.id,
                        text = request.text,
                        rating = request.rating,
                        userID = userID
                    ).fold(
                        ifLeft = { failure ->
                            call.respond(HttpStatusCode.BadRequest, failure.message)
                            return@put
                        },
                        ifRight = {
                            call.respond(HttpStatusCode.OK, SUCCESS_CREATED_FEEDBACK)
                            return@put
                        }
                    )
                }
            )
        }
    }
}