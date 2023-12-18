package ru.cgstore.storage.render_model

import arrow.core.Either
import ru.cgstore.models.Failure
import ru.cgstore.models.render_model.RenderModelDTO
import ru.cgstore.requests.render_models.CreateRenderModelRequest
import ru.cgstore.requests.render_models.UpdateModelRequest

interface RenderModelService {
    suspend fun create(
        login: String,
        request: CreateRenderModelRequest
    ): Either<Failure, Unit>

    suspend fun readAll(page: Long, size: Int): Either<Failure, List<RenderModelDTO>>
    suspend fun readByUserLogin(login: String, page: Long, size: Int): Either<Failure, List<RenderModelDTO>>
    suspend fun readByID(id: String): Either<Failure, RenderModelDTO>
    suspend fun update(
        id: String, request: UpdateModelRequest
    ): Either<Failure, Unit>
    suspend fun delete(id: String): Either<Failure, Unit>
}