package ru.cgstore.models.feedback

import kotlinx.serialization.Serializable

@Serializable
data class FeedBackDTO(
    val id: String,
    val user_id: String,
    val model_id: String,
    val text: String,
    val rating: Double,
    val timestamp: String
)
