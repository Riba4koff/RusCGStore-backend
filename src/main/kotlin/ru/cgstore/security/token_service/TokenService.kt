package ru.cgstore.security.token_service

interface TokenService {
    fun generate(config: TokenConfig, vararg claims: TokenClaim): String
}