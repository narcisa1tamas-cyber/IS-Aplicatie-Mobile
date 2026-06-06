package com.example.is_aplicatie_mobile.model

data class DetaliiLivrare(
    val id: String,
    val idComanda: Int,
    val idPat: Int? = null,
    val nrSalon: Int,
    val numePacient: String,
    val pat: String,
    val medicament: String,
    val status: String,
    val confirmatAsistenta: Boolean = false
)
