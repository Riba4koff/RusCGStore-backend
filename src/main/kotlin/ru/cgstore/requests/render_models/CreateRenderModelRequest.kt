package ru.cgstore.requests.render_models

import kotlinx.serialization.Serializable

@Serializable
data class CreateRenderModelRequest(
    val cost: Double,
    val polygons: Long,
    val vertices: Long
)
