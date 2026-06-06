package com.example.is_aplicatie_mobile.model

import com.google.gson.annotations.SerializedName

data class Alarma(
    @SerializedName("idAlarma")
    val idAlarma: Int,
    @SerializedName("tip")
    val tip: String,
    @SerializedName("mesaj")
    val mesaj: String = "",
    @SerializedName("oraAlarma")
    val oraAlarma: String = "",
    @SerializedName("rezolvata")
    val rezolvata: Boolean = false
)
