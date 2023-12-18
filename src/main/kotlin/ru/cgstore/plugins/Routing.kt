package ru.cgstore.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.routing.*
import io.swagger.codegen.v3.generators.html.StaticHtmlCodegen
import ru.cgstore.security.hash_service.HashingService
import ru.cgstore.security.token_service.TokenConfig
import ru.cgstore.security.token_service.TokenService
import ru.cgstore.storage.users.UsersService
import org.koin.ktor.ext.inject
import ru.cgstore.routes.auth.*
import ru.cgstore.routes.feedback.feedback
import ru.cgstore.routes.profile.provideProfile
import ru.cgstore.routes.render_models.renderModels
import ru.cgstore.storage.feedback.FeedBackService
import ru.cgstore.storage.render_model.RenderModelService

fun Application.configureRouting(config: TokenConfig) {
    val usersService: UsersService by inject()
    val hashingService: HashingService by inject()
    val tokenService: TokenService by inject()
    val feedBackService: FeedBackService by inject()
    val renderModelService: RenderModelService by inject()

    routing {
        auth(
            usersService = usersService,
            tokenService = tokenService,
            hashingService = hashingService,
            tokenConfig = config
        )
        test(usersService = usersService)
        feedback(
            feedBackService = feedBackService,
            usersService = usersService,
            renderModelService = renderModelService
        )
        renderModels(
            renderModelService = renderModelService,
            userService = usersService
        )
        provideProfile(
            usersService = usersService,
            renderModelService = renderModelService
        )
    }
}