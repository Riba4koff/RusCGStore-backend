package ru.cgstore.routes.profile

import io.ktor.resources.*

@Resource("profile")
class Profile {
    @Resource("models")
    data class Models(val profile: Profile = Profile(), val page: Long = 1, val size: Int = 16)
    @Resource("update")
    data class Update(val profile: Profile = Profile())
}