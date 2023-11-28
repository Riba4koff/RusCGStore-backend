package ru.cgstore.requests.users

import kotlinx.serialization.Serializable

@Serializable
data class SignInUserRequest(
    val login: String,
    val password: String
)
