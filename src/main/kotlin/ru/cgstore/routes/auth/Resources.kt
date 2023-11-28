package ru.cgstore.routes.auth

import io.ktor.resources.*
@Resource("/auth")
class Auth {
    @Resource("sign_in")
    class SignIn()

    @Resource("sign_up")
    class SignUp()
}