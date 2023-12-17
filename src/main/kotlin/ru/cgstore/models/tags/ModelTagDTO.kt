package ru.cgstore.models.tags

import kotlinx.serialization.Serializable

@Serializable
data class ModelTagDTO(
    val id: String,
    val model_id: String,
    val name: String
)
