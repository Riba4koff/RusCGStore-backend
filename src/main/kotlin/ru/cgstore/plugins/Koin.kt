package ru.cgstore.plugins

import io.ktor.server.application.*
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.ktor.plugin.KoinApplicationStarted
import org.koin.ktor.plugin.KoinApplicationStopPreparing
import org.koin.ktor.plugin.KoinApplicationStopped
import org.koin.logger.SLF4JLogger
import ru.cgstore.di.appModule
import ru.cgstore.di.securityModule
import ru.cgstore.di.storageModule

// Config class
class KoinPluginConfiguration {
    var appDeclaration: KoinAppDeclaration = {}
        private set

    fun koin(block: KoinAppDeclaration) {
        appDeclaration = block
    }
}

// Plugin
val KoinPlugin = createApplicationPlugin(name = "KoinPlugin", createConfiguration = ::KoinPluginConfiguration) {
    val monitor = environment?.monitor
    val koinApplication = startKoin(appDeclaration = pluginConfig.appDeclaration)
    monitor?.raise(KoinApplicationStarted, koinApplication)

    monitor?.subscribe(ApplicationStopping) {
        monitor.raise(KoinApplicationStopPreparing, koinApplication)
        stopKoin()
        monitor.raise(KoinApplicationStopped, koinApplication)
    }
}

// Usage
fun Application.configureKoin() {
    install(KoinPlugin) {
        koin {
            logger(SLF4JLogger())
            modules(
                storageModule,
                securityModule
            )
        }
    }
}