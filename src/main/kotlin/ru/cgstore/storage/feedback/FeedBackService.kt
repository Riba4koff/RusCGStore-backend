package ru.cgstore.storage.feedback

import arrow.core.Either
import ru.cgstore.models.Failure
import ru.cgstore.models.feedback.FeedBackDTO

interface FeedBackService {
    suspend fun create(
        text: String,
        rating: Double,
        userID: String,
        modelID: String
    ): Either<Failure, Unit>
    suspend fun getAllByModelID(
        model_id: String,
        size: Int,
        page: Int
    ): Either<Failure, List<FeedBackDTO>>
    suspend fun getFeedBackByID(id: String): Either<Failure, FeedBackDTO?>
    suspend fun delete(id: String): Either<Failure, Unit>
}