package ru.cgstore.models

import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    val message: String,
    val data: T? = null
)

@Serializable
data class PageResponse<T>(
    val message: String,
    val data: T? = null,
    val page: Long,
    val size: Int,
    val number_of_found: Int
)
