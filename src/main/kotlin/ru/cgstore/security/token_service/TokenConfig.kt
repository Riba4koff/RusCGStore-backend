package ru.cgstore.security.token_service

data class TokenConfig(
    val audience: String,
    val issuer: String,
    val expiresIn: Long,
    val secret: String
)
