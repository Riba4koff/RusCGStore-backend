﻿# RusCGStore-backend

#SignUp
POST http://localhost:8080/auth/signup
Content-Type: application/json

{
    "login" : "user",
    "email" : "test@gmail.com",
    "phone": "+78005553535",
    "birthday" : "01.01.1970"
    "password" : "12345789"
}
