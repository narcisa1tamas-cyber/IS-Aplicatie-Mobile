package com.example.is_aplicatie_mobile.model

import com.google.gson.annotations.SerializedName

data class Salon(
    @SerializedName("idPat")
    val idPat: Int,

    @SerializedName("nrSalon")
    val nrSalon: Int,

    val ocupat: Boolean
)