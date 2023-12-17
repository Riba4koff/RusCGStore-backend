package ru.cgstore

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import ru.cgstore.plugins.*
import ru.cgstore.security.hash_service.Sha256HashingService
import ru.cgstore.security.token_service.JwtTokenService
import ru.cgstore.security.token_service.TokenConfig
import ru.cgstore.storage.DataBaseConfig
import ru.cgstore.storage.users.UsersServiceImpl

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {

    }
}
