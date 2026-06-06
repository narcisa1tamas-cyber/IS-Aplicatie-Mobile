package com.example.is_aplicatie_mobile.session

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private lateinit var prefs: SharedPreferences

    private val _sessionExpirata = MutableStateFlow(false)
    val sessionExpirata = _sessionExpirata.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("hospihelp_prefs", Context.MODE_PRIVATE)
    }

    fun salveazaSesiune(token: String, rol: String, nume: String, prenume: String) {
        prefs.edit()
            .putString("token", token)
            .putString("rol", rol)
            .putString("nume", nume)
            .putString("prenume", prenume)
            .apply()
        _sessionExpirata.value = false
    }

    fun stergeSesiune() {
        prefs.edit().clear().apply()
        _sessionExpirata.value = false
    }

    fun marcheazaSesiuneExpirata() {
        stergeSesiune()
        _sessionExpirata.value = true
    }

    fun getToken(): String    = prefs.getString("token", "")   ?: ""
    fun getRol(): String      = prefs.getString("rol", "")     ?: ""
    fun getNume(): String     = prefs.getString("nume", "")    ?: ""
    fun getPrenume(): String  = prefs.getString("prenume", "") ?: ""
    fun esteLogat(): Boolean  = getToken().isNotBlank()
}
