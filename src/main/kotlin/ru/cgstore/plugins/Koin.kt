package ru.cgstore.plugins

import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import ru.cgstore.di.security_module

fun Application.koin(){
    install(Koin) {
        modules(security_module)
    }
}