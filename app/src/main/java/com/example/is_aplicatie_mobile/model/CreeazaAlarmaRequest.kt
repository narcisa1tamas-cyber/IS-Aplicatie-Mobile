package com.example.is_aplicatie_mobile.model

import com.google.gson.annotations.SerializedName

data class CreeazaAlarmaRequest(
    @SerializedName("tip")
    val tip: String,
    @SerializedName("mesaj")
    val mesaj: String,
    @SerializedName("idComanda")
    val idComanda: Int? = null
)
