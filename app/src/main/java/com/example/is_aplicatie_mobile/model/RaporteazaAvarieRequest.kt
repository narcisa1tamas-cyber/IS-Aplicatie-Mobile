package com.example.is_aplicatie_mobile.model

import com.google.gson.annotations.SerializedName

data class RaporteazaAvarieRequest(
    @SerializedName("tip")
    val tip: String,
    @SerializedName("descriere")
    val descriere: String
)
