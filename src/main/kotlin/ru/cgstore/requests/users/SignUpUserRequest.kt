package ru.cgstore.requests.users

import kotlinx.serialization.Serializable

@Serializable
data class SignUpUserRequest(
    val login: String,
    val email: String,
    val phone: String,
    val birthday: String,
    val password: String,
)
