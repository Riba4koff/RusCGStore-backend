package ru.cgstore.responses.profile

import kotlinx.serialization.Serializable
import ru.cgstore.models.users.UserRole

@Serializable
data class ProfileDataResponse(
    val login: String,
    val email: String,
    val phone: String,
    val birthday: String,
    val role: UserRole,
    val banned: Boolean
)
