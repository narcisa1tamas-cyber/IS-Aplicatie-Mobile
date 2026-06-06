package com.example.is_aplicatie_mobile.network

import android.util.Log
import com.example.is_aplicatie_mobile.session.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://hospihelp-production.up.railway.app/"

    val instance: HospiHelpApiService by lazy {
        val logging = HttpLoggingInterceptor { message ->
            Log.d("API_RAW", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            if (response.code == 401) {
                Log.w("RetrofitClient", "401 Unauthorized — sesiune expirată")
                SessionManager.marcheazaSesiuneExpirata()
            }
            response
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(HospiHelpApiService::class.java)
    }
}
