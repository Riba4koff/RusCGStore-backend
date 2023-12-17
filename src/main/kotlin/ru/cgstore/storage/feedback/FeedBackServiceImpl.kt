package ru.cgstore.storage.feedback

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import ru.cgstore.models.Failure
import ru.cgstore.models.feedback.FeedBackDTO
import ru.cgstore.storage.feedback.FeedBackServiceImpl.FeedBackTable.id
import ru.cgstore.storage.feedback.FeedBackServiceImpl.FeedBackTable.model_id
import ru.cgstore.storage.feedback.FeedBackServiceImpl.FeedBackTable.rating
import ru.cgstore.storage.feedback.FeedBackServiceImpl.FeedBackTable.text
import ru.cgstore.storage.feedback.FeedBackServiceImpl.FeedBackTable.timestamp
import ru.cgstore.storage.feedback.FeedBackServiceImpl.FeedBackTable.user_id
import java.util.*

class FeedBackServiceImpl(database: Database) : FeedBackService {
    private companion object {
        const val FAILED_TO_CREATE_FEEDBACK = "Не удалось создать отзыв"
        const val FAILED_TO_DELETE_FEEDBACK = "Не удалось удалить отзыв"
        const val FEEDBACK_NOT_FOUND = "Отзыв не найден"
    }

    object FeedBackTable : Table("table") {
        val id = varchar("id", 36)
        val user_id = varchar("user_id", 36)
        val model_id = varchar("model_id", 36)
        val text = text("text")
        val rating = double("rating")
        val timestamp = varchar("timestamp", 64)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(FeedBackTable)
        }
    }

    private suspend fun <T> dbQuery(block: () -> T) = newSuspendedTransaction(Dispatchers.IO) {
        block()
    }

    override suspend fun create(text: String, rating: Double, userID: String, modelID: String): Either<Failure, Unit> =
        Either.catch {
            dbQuery {
                val createdFeedBackID = UUID.randomUUID().toString()
                val timestampOfCreate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                FeedBackTable.insert {
                    it[id] = createdFeedBackID
                    it[user_id] = userID
                    it[FeedBackTable.model_id] = modelID
                    it[FeedBackTable.text] = text
                    it[FeedBackTable.rating] = rating
                    it[timestamp] = timestampOfCreate
                }
                Unit
            }
        }.mapLeft { failure ->
            failure.printStackTrace()
            Failure.CreateFailure(FAILED_TO_CREATE_FEEDBACK)
        }

    override suspend fun getAllByModelID(
        model_id: String,
        size: Int,
        page: Long,
    ): Either<Failure, List<FeedBackDTO>> = Either.catch {
        dbQuery {
            FeedBackTable.select {
                FeedBackTable.model_id eq model_id
            }.limit(size, (page - 1) * size).map(::resultRowToDTO)
        }
    }.mapLeft { failure ->
        failure.printStackTrace()
        Failure.ReadFailure(message = FAILED_TO_CREATE_FEEDBACK)
    }

    override suspend fun getFeedBackByID(id: String): Either<Failure, FeedBackDTO?> = Either.catch {
        dbQuery {
            FeedBackTable.select {
                FeedBackTable.id eq id
            }.map(::resultRowToDTO).singleOrNull()
        }
    }.mapLeft { failure ->
        failure.printStackTrace()
        Failure.ReadFailure(message = FAILED_TO_CREATE_FEEDBACK)
    }

    override suspend fun delete(id: String): Either<Failure, Unit> = Either.catch {
        val model = dbQuery { FeedBackTable.select { FeedBackTable.id eq id }.singleOrNull() }
        if (model == null) return Failure.DeleteFailure(FEEDBACK_NOT_FOUND).left() else {
            dbQuery { FeedBackTable.deleteWhere { FeedBackTable.id eq id } }
            Unit
        }
    }.mapLeft { failure ->
        failure.printStackTrace()
        Failure.ReadFailure(message = FAILED_TO_DELETE_FEEDBACK)
    }

    private fun resultRowToDTO(resultRow: ResultRow) = FeedBackDTO(
        id = resultRow[id],
        user_id = resultRow[user_id],
        model_id = resultRow[model_id],
        text = resultRow[text],
        rating = resultRow[rating],
        timestamp = resultRow[timestamp]
    )
}