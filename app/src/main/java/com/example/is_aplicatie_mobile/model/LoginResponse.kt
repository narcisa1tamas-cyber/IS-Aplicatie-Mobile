package com.example.is_aplicatie_mobile.model

data class LoginResponse(
    val token: String,
    val email: String,
    val rol: String,
    val nume: String,
    val prenume: String
)