package ru.cgstore.storage.users

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import ru.cgstore.models.Failure
import ru.cgstore.models.users.User
import ru.cgstore.requests.users.SignUpUserRequest
import ru.cgstore.security.hash_service.SaltedHash
import ru.cgstore.storage.users.UsersServiceImpl.UsersTable.banned
import ru.cgstore.storage.users.UsersServiceImpl.UsersTable.birthday
import ru.cgstore.storage.users.UsersServiceImpl.UsersTable.email
import ru.cgstore.storage.users.UsersServiceImpl.UsersTable.hash
import ru.cgstore.storage.users.UsersServiceImpl.UsersTable.id
import ru.cgstore.storage.users.UsersServiceImpl.UsersTable.login
import ru.cgstore.storage.users.UsersServiceImpl.UsersTable.phone
import ru.cgstore.storage.users.UsersServiceImpl.UsersTable.role
import ru.cgstore.storage.users.UsersServiceImpl.UsersTable.salt
import ru.cgstore.storage.users.UsersServiceImpl.UsersTable.timestamp
import java.util.*

class UsersServiceImpl(database: Database) : UsersService {
    object UsersTable : Table("users") {
        val id = varchar("id", 36)
        val email = varchar("email", 64)
        val login = varchar("login", 32)
        val phone = varchar("phone", 12)
        val timestamp = varchar("timestamp", 29)
        val hash = varchar("hash", 100)
        val salt = varchar("salt", 100)
        val role = varchar("role", 12)
        val banned = bool("banned")
        val birthday = varchar("birthday", 32)
    }

    init {
        transaction(database) {
            SchemaUtils.create(UsersTable)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T) = newSuspendedTransaction(Dispatchers.IO) {
        block()
    }

    override suspend fun create(
        request: SignUpUserRequest,
        saltedHash: SaltedHash
    ): Either<Failure, Unit> = Either.catch {
        dbQuery {
            val timeOfCreate = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            UsersTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[login] = request.login
                it[email] = request.email
                it[phone] = request.phone
                it[birthday] = request.birthday
                it[hash] = saltedHash.hash
                it[salt] = saltedHash.salt
                it[timestamp] = timeOfCreate.toString()
            }
            Unit
        }
    }.mapLeft { error -> Failure.CreateFailure(error.message.orEmpty()) }

    override suspend fun loginExists(login: String): Either<Failure, Unit> = either<Failure, Unit> {
        val loginExists = dbQuery { dbQuery { UsersTable.select { UsersTable.login eq login }.singleOrNull() == null } }
        ensure(loginExists) {
            Failure.ValidationFailure("User with login: $login already exists.")
        }
    }.mapLeft { error -> Failure.ReadFailure(error.message) }

    override suspend fun emailExists(email: String): Either<Failure, Unit> = either<Failure, Unit> {
        val emailExists = dbQuery { UsersTable.select { UsersTable.email eq email }.singleOrNull() == null }
        ensure(emailExists) {
            Failure.ValidationFailure("User with email: $email already exists.")
        }
    }.mapLeft { error -> Failure.ReadFailure(error.message.orEmpty()) }

    override suspend fun phoneExists(phone: String): Either<Failure, Unit> = either<Failure, Unit> {
        val emailExists = dbQuery { UsersTable.select { UsersTable.phone eq phone }.singleOrNull() == null }
        ensure(emailExists) {
            Failure.ValidationFailure("User with phone: $phone already exists.")
        }
    }.mapLeft { error -> Failure.ReadFailure(error.message.orEmpty()) }

    override suspend fun read(id: String): Either<Failure, User> = either<Failure, User> {
        val user = dbQuery { UsersTable.select { UsersTable.id eq id } }.map(::resultRowToUser).singleOrNull()
        ensure(user != null) { Failure.ReadFailure("User with id: $id was not found.") }
        user
    }.mapLeft { error -> Failure.ReadFailure(error.message) }

    override suspend fun readByLogin(login: String): Either<Failure, User> = either<Failure, User> {
        dbQuery {
            val resultRow = UsersTable.select { UsersTable.login eq login}.firstOrNull()
            ensure(resultRow != null) { Failure.ReadFailure("User with login: $login was not found.") }
            resultRowToUser(resultRow)
        }
    }.mapLeft { error -> Failure.ReadFailure(error.message) }

    override suspend fun read(): Either<Failure, List<User>> = Either.catch {
        TODO("Not yet implemented")
    }.mapLeft { error -> Failure.ReadFailure(error.message.orEmpty()) }

    override suspend fun update(): Either<Failure, Unit> = Either.catch {
        TODO("Not yet implemented")
    }.mapLeft { error -> Failure.UpdateFailure(error.message.orEmpty()) }

    override suspend fun setRole(): Either<Failure, Unit> = Either.catch {
        TODO("Not yet implemented")
    }.mapLeft { error -> Failure.UpdateFailure(error.message.orEmpty()) }

    override suspend fun block(id: String): Either<Failure, Unit> = Either.catch {
        UsersTable.update(where = { UsersTable.id eq id }) {
            it[banned] = true
        }
        Unit
    }.mapLeft { error -> Failure.UpdateFailure(error.message.orEmpty()) }

    override suspend fun unblock(id: String): Either<Failure, Unit> = Either.catch {
        UsersTable.update(where = { UsersTable.id eq id }) {
            it[banned] = false
        }
        Unit
    }.mapLeft { error -> Failure.UpdateFailure(error.message.orEmpty()) }

    private fun resultRowToUser(resultRow: ResultRow) = User(
        id = resultRow[id],
        login = resultRow[login],
        email = resultRow[email],
        phone = resultRow[phone],
        timestamp = resultRow[timestamp],
        hash = resultRow[hash],
        salt = resultRow[salt],
        birthday = resultRow[birthday],
        role = resultRow[role],
        banned = resultRow[banned]
    )
}