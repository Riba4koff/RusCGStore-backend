package ru.cgstore.models.render_model

import kotlinx.serialization.Serializable

@Serializable
data class RenderModelDTO(
    val id: String,
    val name: String,
    val description: String,
    val p_date: String,
    val author_login: String,
    val cost: Double,
    val polygons: Long,
    val vertices: Long
)
