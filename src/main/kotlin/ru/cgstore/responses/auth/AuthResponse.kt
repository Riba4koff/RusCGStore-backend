package ru.cgstore.responses.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String
)
