package ru.cgstore.storage.model_formats

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ModelFormatServiceImpl(database: Database): ModelFormatService {
    object ModelFormatTable: Table("model_format_table") {
        val id = varchar("id", 36)
        val model_id = varchar("model_id", 36)
        val name = varchar("name", 64)
        val size = integer("size")
        val link = varchar("link", 256)

        override val primaryKey = PrimaryKey(id)
    }
    private suspend fun <T>dbQuery(block: () -> T) = newSuspendedTransaction(Dispatchers.IO) {
        block()
    }
}