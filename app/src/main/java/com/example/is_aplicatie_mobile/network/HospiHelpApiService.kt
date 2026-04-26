package com.example.is_aplicatie_mobile.network

import com.example.is_aplicatie_mobile.model.Comanda
import com.example.is_aplicatie_mobile.model.LoginRequest
import com.example.is_aplicatie_mobile.model.LoginResponse
import com.example.is_aplicatie_mobile.model.Salon
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HospiHelpApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/paturi")
    suspend fun getSaloane(
        @Header("Authorization") token: String
    ): Response<List<Salon>>

    // Aici trimitem nrSalon, nu idPat
    @GET("api/comenzi/salon/{nrSalon}/active")
    suspend fun getComenziBySalon(
        @Header("Authorization") token: String,
        @Path("nrSalon") nrSalon: Int
    ): Response<List<Comanda>>

    @PUT("api/comenzi/{id}/status")
    suspend fun actualizeazaStatusComanda(
        @Header("Authorization") token: String,
        @Path("id") idComanda: Int,
        @Body body: Map<String, String>
    ): Response<Comanda>

    @GET("api/comenzi/status/ACTIV") // Sau adresa exactă pe care ai pus-o în Controller
    suspend fun getToateComenzileActive(
        @Header("Authorization") token: String
    ): Response<List<Comanda>>

    @GET("api/comenzi/transport-curent") // FĂRĂ salon în coadă!
    suspend fun getTransportCurent(
        @Header("Authorization") token: String
    ): Response<List<Comanda>>
}