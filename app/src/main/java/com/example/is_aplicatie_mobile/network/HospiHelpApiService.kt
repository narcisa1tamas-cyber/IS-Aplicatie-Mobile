package com.example.is_aplicatie_mobile.network

import com.example.is_aplicatie_mobile.model.ActualizareComandaRequest
import com.example.is_aplicatie_mobile.model.Alarma
import com.example.is_aplicatie_mobile.model.CreeazaAlarmaRequest
import com.example.is_aplicatie_mobile.model.Comanda
import com.example.is_aplicatie_mobile.model.LoginRequest
import com.example.is_aplicatie_mobile.model.LoginResponse
import com.example.is_aplicatie_mobile.model.Salon
import com.example.is_aplicatie_mobile.model.TeleghidareOperatorRequest
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

    @GET("api/comenzi/salon/{nrSalon}/active")
    suspend fun getComenziBySalon(
        @Header("Authorization") token: String,
        @Path("nrSalon") nrSalon: Int
    ): Response<List<Comanda>>

    @PUT("api/comenzi/{id}/status")
    suspend fun actualizeazaStatusComanda(
        @Header("Authorization") token: String,
        @Path("id") idComanda: Int,
        @Body body: ActualizareComandaRequest
    ): Response<Comanda>

    @PUT("api/comenzi/{id}/confirma")
    suspend fun confirmaAjungereAsistenta(
        @Header("Authorization") token: String,
        @Path("id") idComanda: Int
    ): Response<String>

    @GET("api/comenzi/status/ACTIV")
    suspend fun getToateComenzileActive(
        @Header("Authorization") token: String
    ): Response<List<Comanda>>

    @GET("api/comenzi/status/IN_ASTEPTARE")
    suspend fun getComenzileInAsteptare(
        @Header("Authorization") token: String
    ): Response<List<Comanda>>

    @GET("api/comenzi/transport-curent")
    suspend fun getTransportCurent(
        @Header("Authorization") token: String
    ): Response<List<Comanda>>

    @GET("api/comenzi")
    suspend fun getToateComenzile(
        @Header("Authorization") token: String
    ): Response<List<Comanda>>

    @GET("api/alarme")
    suspend fun getAlarme(
        @Header("Authorization") token: String
    ): Response<List<Alarma>>

    @POST("api/alarme")
    suspend fun creeazaAlarma(
        @Header("Authorization") token: String,
        @Body body: CreeazaAlarmaRequest
    ): Response<Alarma>

    @PUT("api/alarme/{id}/rezolva")
    suspend fun rezolvaAlarma(
        @Header("Authorization") token: String,
        @Path("id") idAlarma: Int
    ): Response<Alarma>

    @POST("api/teleghidare/operator")
    suspend fun transmiteOperatorTeleghidare(
        @Header("Authorization") token: String,
        @Body body: TeleghidareOperatorRequest
    ): Response<Void>
}