package com.example.is_aplicatie_mobile.model

data class DetaliiLivrare(
    val id: String, // Schimbăm aici în String
    val numePacient: String,
    val pat: String,
    val medicament: String,
    var status: String
)