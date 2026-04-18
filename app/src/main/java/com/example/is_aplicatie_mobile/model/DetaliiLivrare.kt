// model/DetaliiLivrare.kt
package com.example.is_aplicatie_mobile.model

data class DetaliiLivrare(
    val id: String, // Identificator unic (ex: CNP sau ID pat) [cite: 1511]
    val numePacient: String,
    val pat: String,
    val medicament: String,
    var status: String // "În așteptare", "Activ", "Finalizat" [cite: 938]
)