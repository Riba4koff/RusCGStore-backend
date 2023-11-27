package ru.cgstore.security.hash_service

data class SaltedHash(
    val hash: String,
    val salt: String
)