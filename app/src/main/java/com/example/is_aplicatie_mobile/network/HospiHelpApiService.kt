package com.example.is_aplicatie_mobile.network

import com.example.is_aplicatie_mobile.model.LoginRequest
import com.example.is_aplicatie_mobile.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface HospiHelpApiService {

    // Aceasta trebuie să se potrivească cu ruta de pe backend-ul Spring Boot
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Pe viitor, aici vei adăuga alte rute, ex:
    // @GET("api/comenzi")
    // suspend fun getComenzi(): Response<List<Comanda>>
}