package ru.cgstore.storage.cart

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class CartServiceImpl(database: Database): CartService {
    object CartTable: Table("cart") {
        val user_id = varchar("user_id", 36)
        val model_id = varchar("model_id", 36)
        val amount = integer("amount")
    }

    private suspend fun <T>dbQuery(block: () -> T) = newSuspendedTransaction(Dispatchers.IO) {
        block()
    }
}