package ru.cgstore.storage.tags

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ModelTagServiceImpl(database: Database): ModelTagService {
    object ModelTagTable: Table("model_tag") {
        val id = varchar("id", 36)
        val model_id = varchar("model_id", 36)
        val name = varchar("name", 64)
    }
    private suspend fun <T>dbQuery(block: () -> T) = newSuspendedTransaction(Dispatchers.IO) {
        block()
    }
}