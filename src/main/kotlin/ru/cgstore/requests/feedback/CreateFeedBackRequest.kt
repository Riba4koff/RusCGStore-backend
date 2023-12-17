package ru.cgstore.requests.feedback

import kotlinx.serialization.Serializable

@Serializable
data class CreateFeedBackRequest(
    val model_id: String,
    val text: String,
    val rating: Double
)
