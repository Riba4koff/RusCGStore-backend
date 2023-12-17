package ru.cgstore.requests.profile

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val email: String? = null,
    val phone: String? = null,
    val birthday: String? = null
)
