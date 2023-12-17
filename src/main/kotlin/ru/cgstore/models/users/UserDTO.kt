package ru.cgstore.models.users

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: String,
    val login: String,
    val email: String,
    val phone: String,
    val birthday: String,
    val role: UserRole,
    val banned: Boolean
)
