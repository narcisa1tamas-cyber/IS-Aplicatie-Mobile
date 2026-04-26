package com.example.is_aplicatie_mobile.model

data class LoginResponse(
    val token: String,    // Primul din Java (token)
    val email: String,    // Al doilea din Java (angajat.getEmail())
    val rol: String,      // Al treilea din Java (angajat.getRol().name())
    val nume: String,     // Al patrulea din Java (angajat.getNume())
    val prenume: String   // Al cincilea din Java (angajat.getPrenume())
)