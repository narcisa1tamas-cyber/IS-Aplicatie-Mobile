package com.example.is_aplicatie_mobile.model

import com.google.gson.annotations.SerializedName

data class Avarie(
    @SerializedName("idAvarie")
    val idAvarie: Int,
    @SerializedName("tip")
    val tip: String,
    @SerializedName("descriere")
    val descriere: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("rezolvat")
    val rezolvat: Boolean = false
)
