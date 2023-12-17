package ru.cgstore.models.users

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole(
    val value: String
) {
    @SerialName("ADMIN")
    ADMIN(value = "Админ"),
    @SerialName("MANAGER")
    MANAGER(value = "Менеджер"),
    @SerialName("DEVELOPER")
    DEVELOPER(value = "Разработчик"),
    @SerialName("USER")
    USER(value = "Пользователь")
}