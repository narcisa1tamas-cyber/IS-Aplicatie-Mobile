package com.example.is_aplicatie_mobile.model

data class LoginResponse(
    val id_angajat: Int,
    val nume: String,
    val rol: String, // ex: "Asistent", "Medic", etc.
    val token: String // Dacă folosesc JWT
)