package ru.cgstore.di

import org.jetbrains.exposed.sql.Database
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.cgstore.security.hash_service.HashingService
import ru.cgstore.security.hash_service.Sha256HashingService
import ru.cgstore.security.token_service.JwtTokenService
import ru.cgstore.security.token_service.TokenService
import ru.cgstore.storage.DataBaseConfig
import ru.cgstore.storage.users.UsersService
import ru.cgstore.storage.users.UsersServiceImpl

val appModule = module {
    storageModule
    securityModule
}

val storageModule = module {
    single {
        val dataBaseConfig = DataBaseConfig(
            user = System.getenv("user"),
            password = System.getenv("password"),
            database = System.getenv("database"),
            ip = System.getenv("ip")
        )
        Database.connect(
            url = "jdbc:postgresql://${dataBaseConfig.ip}/${dataBaseConfig.database}",
            driver = "org.postgresql.Driver",
            user = dataBaseConfig.user,
            password = dataBaseConfig.password
        )
    }
    single<UsersService> { UsersServiceImpl(get()) }
}

val securityModule = module {
    factoryOf(::Sha256HashingService) { bind<HashingService>() }
    factoryOf(::JwtTokenService) { bind<TokenService>() }
}