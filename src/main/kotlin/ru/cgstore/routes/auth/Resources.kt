package ru.cgstore.routes.auth

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import ru.cgstore.requests.users.SignInUserRequest

@Resource("auth")
class Auth {
    @Resource("sign_in")
    class SignIn(val parent: Auth)

    @Resource("sign_up")
    class SignUp(val parent: Auth)
}