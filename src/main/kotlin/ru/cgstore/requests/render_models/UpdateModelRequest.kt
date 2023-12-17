package ru.cgstore.requests.render_models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateModelRequest(
    val name: String? = null,
    val description: String? = null,
    val cost: Double? = null,
    val polygons: Long? = null,
    val vertices: Long? = null,
)
