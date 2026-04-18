package com.example.is_aplicatie_mobile.model

data class Comanda(
    val id_comanda: Int,
    val status: String, // "În aşteptare", "Activ", "Finalizat", "Nefinalizat" [cite: 860]
    val id_pat: Int,
    val id_prescriptie: Int
)