package ru.cgstore.models

import io.ktor.http.*

sealed interface Failure {

    val message: String

    data class ValidationFailure(
        override val message: String,
        val statusCode: HttpStatusCode = HttpStatusCode.BadRequest,
    ) : Failure

    data class RuntimeError(override val message: String) : Failure
    data class CreateFailure(override val message: String) : Failure
    data class ReadFailure(
        val statusCode: HttpStatusCode = HttpStatusCode.NotFound,
        override val message: String,
    ) : Failure

    data class UpdateFailure(
        override val message: String,
    ) : Failure

    data class DeleteFailure(override val message: String) : Failure
}