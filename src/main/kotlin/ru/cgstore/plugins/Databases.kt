package ru.cgstore.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import ru.cgstore.storage.DataBaseConfig

fun Application.configureDatabases(config: DataBaseConfig): Database = Database.connect(
    url = "jdbc:postgresql://${config.ip}/${config.database}",
    driver = "org.postgresql.Driver",
    user = config.user,
    password = config.password
)
