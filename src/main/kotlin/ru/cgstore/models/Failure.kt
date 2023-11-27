package ru.cgstore.models

import io.ktor.http.*

sealed interface Failure {
    val message: String

    data class ValidationFailure(
        override val message: String,
        val statusCode: HttpStatusCode = HttpStatusCode.BadRequest
    ) : Failure

    data class GenericFailure(val e: Exception) : Failure {
        override val message: String = e.localizedMessage
    }

    data class RuntimeError(override val message: String) : Failure
    data class CreateFailure(override val message: String) : Failure
    data class ReadFailure(override val message: String) : Failure
    data class UpdateFailure(
        override val message: String,
    ) : Failure
}