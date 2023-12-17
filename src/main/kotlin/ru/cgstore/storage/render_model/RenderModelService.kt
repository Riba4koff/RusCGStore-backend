package ru.cgstore.storage.render_model

import arrow.core.Either
import arrow.core.raise.Raise
import ru.cgstore.models.Failure
import ru.cgstore.models.render_model.RenderModelDTO
import ru.cgstore.requests.render_models.UpdateModelRequest

interface RenderModelService {
    suspend fun create(
        author_id: String,
        cost: Double,
        polygons: Long,
        vertices: Long,
    ): Either<Failure, Unit>

    suspend fun readAll(): Either<Failure, List<RenderModelDTO>>
    suspend fun readByUserID(author_id: String): Either<Failure, List<RenderModelDTO>>
    suspend fun readByID(id: String): Either<Failure, RenderModelDTO>
    suspend fun update(
        id: String, request: UpdateModelRequest
    ): Either<Failure, Unit>
    suspend fun delete(id: String): Either<Failure, Unit>
}