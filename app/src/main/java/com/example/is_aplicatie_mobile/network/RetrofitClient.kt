package com.example.is_aplicatie_mobile.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // ÎNLOCUIEȘTE CU IP-UL SAU URL-UL BACKEND-ULUI TĂU
    // Dacă rulezi pe emulator, folosește IP-ul calculatorului tău http://192.168.1.101:8081/ sau "http://10.0.2.2:8080/" pentru localhost
    private const val BASE_URL = "http://192.168.1.131:8081/"

    val instance: HospiHelpApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(HospiHelpApiService::class.java)
    }
}