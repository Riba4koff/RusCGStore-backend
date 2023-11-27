package ru.cgstore.security.token_service

data class TokenClaim(
    val name: String,
    val value: String
)