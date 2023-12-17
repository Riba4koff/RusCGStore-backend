package ru.cgstore.di

import org.jetbrains.exposed.sql.Database
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import ru.cgstore.security.hash_service.HashingService
import ru.cgstore.security.hash_service.Sha256HashingService
import ru.cgstore.security.token_service.JwtTokenService
import ru.cgstore.security.token_service.TokenService
import ru.cgstore.storage.DataBaseConfig
import ru.cgstore.storage.users.UsersService
import ru.cgstore.storage.users.UsersServiceImpl
import ru.cgstore.storage.cart.CartService
import ru.cgstore.storage.cart.CartServiceImpl
import ru.cgstore.storage.feedback.FeedBackServiceImpl
import ru.cgstore.storage.feedback.FeedBackService
import ru.cgstore.storage.model_formats.ModelFormatService
import ru.cgstore.storage.model_formats.ModelFormatServiceImpl
import ru.cgstore.storage.render_model.RenderModelService
import ru.cgstore.storage.render_model.RenderModelServiceImpl
import ru.cgstore.storage.tags.ModelTagService
import ru.cgstore.storage.tags.ModelTagServiceImpl

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
    single<CartService> { CartServiceImpl(get()) }
    single<FeedBackService> { FeedBackServiceImpl(get()) }
    single<ModelFormatService> { ModelFormatServiceImpl(get()) }
    single<RenderModelService> { RenderModelServiceImpl(get()) }
    single<ModelTagService> { ModelTagServiceImpl(get()) }
}

val securityModule = module {
    factoryOf(::Sha256HashingService) { bind<HashingService>() }
    factoryOf(::JwtTokenService) { bind<TokenService>() }
}