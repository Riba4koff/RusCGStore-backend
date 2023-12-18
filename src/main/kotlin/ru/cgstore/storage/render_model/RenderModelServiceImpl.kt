package ru.cgstore.storage.render_model

import arrow.core.Either
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
import ru.cgstore.models.render_model.RenderModelDTO
import ru.cgstore.requests.render_models.CreateRenderModelRequest
import ru.cgstore.requests.render_models.UpdateModelRequest
import ru.cgstore.storage.render_model.RenderModelServiceImpl.RenderModelTable.author_login
import ru.cgstore.storage.render_model.RenderModelServiceImpl.RenderModelTable.cost
import ru.cgstore.storage.render_model.RenderModelServiceImpl.RenderModelTable.description
import ru.cgstore.storage.render_model.RenderModelServiceImpl.RenderModelTable.id
import ru.cgstore.storage.render_model.RenderModelServiceImpl.RenderModelTable.name
import ru.cgstore.storage.render_model.RenderModelServiceImpl.RenderModelTable.p_date
import ru.cgstore.storage.render_model.RenderModelServiceImpl.RenderModelTable.polygons
import ru.cgstore.storage.render_model.RenderModelServiceImpl.RenderModelTable.vertices
import java.util.UUID

class RenderModelServiceImpl(database: Database) : RenderModelService {
    private companion object {
        const val RENDER_MODEL_WAS_NOT_FOUND = "Модель не найдена"
    }

    object RenderModelTable : Table("render_model") {
        val id = varchar("id", 36)
        val name = varchar("name", 64)
        val description = varchar("description", 512)
        val author_login = varchar("author_login", 36)
        val p_date = varchar("p_date", 64)
        val cost = double("double")
        val polygons = long("polygons")
        val vertices = long("vertices")

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    private suspend fun <T> dbQuery(block: () -> T) = newSuspendedTransaction(Dispatchers.IO) {
        block()
    }

    init {
        transaction(database) {
            SchemaUtils.create(RenderModelTable)
        }
    }

    override suspend fun create(
        login: String,
        request: CreateRenderModelRequest,
    ): Either<Failure, Unit> = Either.catch {
        val timeOfCreate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
        dbQuery {
            RenderModelTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[p_date] = timeOfCreate
                it[RenderModelTable.author_login] = login
                it[name] = request.name
                it[description] = request.description
                it[cost] = request.cost
                it[polygons] = request.polygons
                it[vertices] = request.vertices
            }
        }
        Unit
    }.mapLeft { Failure.CreateFailure(it.message.orEmpty()) }

    override suspend fun readAll(page: Long, size: Int): Either<Failure, List<RenderModelDTO>> = Either.catch {
        dbQuery {
            RenderModelTable.selectAll().limit(size, (page - 1) * size).map(::resultRowToDTO)
        }
    }.mapLeft { Failure.ReadFailure(message = it.message.orEmpty()) }

    override suspend fun readByUserLogin(login: String, page: Long, size: Int): Either<Failure, List<RenderModelDTO>>  =
        Either.catch {
            dbQuery {
                RenderModelTable.select {
                    RenderModelTable.author_login eq login
                }.limit(size, (page - 1) * size).map(::resultRowToDTO)
            }
        }.mapLeft { failure -> Failure.ReadFailure(message = failure.message.orEmpty()) }

    override suspend fun readByID(id: String): Either<Failure, RenderModelDTO> = either {
        val model = dbQuery { RenderModelTable.select { RenderModelTable.id eq id }.singleOrNull() }
        ensure(model != null) { Failure.ReadFailure(message = RENDER_MODEL_WAS_NOT_FOUND) }
        model.let(::resultRowToDTO)
    }.mapLeft { Failure.ReadFailure(message = it.message) }

    override suspend fun update(id: String, request: UpdateModelRequest) = Either.catch {
        dbQuery {
            RenderModelTable.update({ RenderModelTable.id eq id }) {
                if (request.name != null) it[name] = request.name
                if (request.description != null) it[description] = request.description
                if (request.cost != null) it[cost] = request.cost
                if (request.polygons != null) it[polygons] = request.polygons
                if (request.vertices != null) it[vertices] = request.vertices
            }
            Unit
        }
    }.mapLeft { Failure.UpdateFailure(it.message.orEmpty()) }

    override suspend fun delete(id: String): Either<Failure, Unit> = either {
        val model = dbQuery { RenderModelTable.select { RenderModelTable.id eq id }.singleOrNull() }
        ensure(model != null) { Failure.DeleteFailure(RENDER_MODEL_WAS_NOT_FOUND) }
        dbQuery { RenderModelTable.deleteWhere { RenderModelTable.id eq id } }
        Unit
    }.mapLeft { Failure.DeleteFailure(it.message) }

    private fun resultRowToDTO(resultRow: ResultRow) = RenderModelDTO(
        id = resultRow[id],
        name = resultRow[name],
        description = resultRow[description],
        p_date = resultRow[p_date],
        author_login = resultRow[author_login],
        cost = resultRow[cost],
        polygons = resultRow[polygons],
        vertices = resultRow[vertices]
    )
}