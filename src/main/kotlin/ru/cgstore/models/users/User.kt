package ru.cgstore.models.users

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val login: String,
    val email: String,
    val phone: String,
    val timestamp: String,
    val hash: String,
    val salt: String,
    val birthday: String,
    val role: String,
    val banned: Boolean
)
