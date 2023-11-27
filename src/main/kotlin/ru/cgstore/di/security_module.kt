package ru.cgstore.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import ru.cgstore.security.hash_service.*
import ru.cgstore.security.token_service.*

val security_module = module {
    factoryOf(::Sha256HashingService) {
        bind<HashingService>()
    }
    factoryOf(::JwtTokenService) {
        bind<TokenService>()
    }
}