package com.example.is_aplicatie_mobile.model

import com.google.gson.annotations.SerializedName
data class Comanda(
    @SerializedName("idComanda")
    val idComanda: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("pat")
    val pat: PatObiect?,
    @SerializedName("prescriptie")
    val prescriptie: PrescriptieObiect?
)

data class PatObiect(
    @SerializedName("idPat")
    val idPat: Int,
    @SerializedName("nrSalon")
    val nrSalon: Int
)

data class PrescriptieObiect(
    @SerializedName("pacient")
    val pacient: PacientObiect?,
    @SerializedName("medicament")
    val medicament: MedicamentObiect?
)

data class PacientObiect(
    @SerializedName("nume") val nume: String,
    @SerializedName("prenume") val prenume: String
)

data class MedicamentObiect(
    @SerializedName("denumire") val denumire: String
)