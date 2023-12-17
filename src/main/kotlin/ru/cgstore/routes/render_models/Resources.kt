package ru.cgstore.routes.render_models

import io.ktor.resources.*

@Resource("models")
class Models {
    @Resource("all")
    data class All(
        val parent: Models = Models(),
        val size: Int = 16,
        val page: Int = 1,
    )
    @Resource("create")
    data class Create(val parent: Models = Models())

    @Resource("{id}")
    data class ID(val parent: Models = Models(), val id: String? = null)
}