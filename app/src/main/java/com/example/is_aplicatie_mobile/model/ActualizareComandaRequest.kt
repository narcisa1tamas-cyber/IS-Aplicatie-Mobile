package com.example.is_aplicatie_mobile.model

import com.google.gson.annotations.SerializedName

data class ActualizareComandaRequest(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("confirmatAsistenta")
    val confirmatAsistenta: Boolean? = null
)
