package ru.cgstore.routes.feedback

import io.ktor.resources.*

@Resource("feedback")
class FeedBack {
    @Resource("create")
    data class Create(val parent: FeedBack = FeedBack())

    @Resource("{id}")
    data class ID(val parent: FeedBack = FeedBack(), val id: String)

    @Resource("all")
    data class All(
        val parent: FeedBack = FeedBack(),
        val modelID: String = "",
        val page: Int = 0,
        val size: Int = 16,
    )
}