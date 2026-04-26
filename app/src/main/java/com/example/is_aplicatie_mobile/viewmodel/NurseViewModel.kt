package com.example.is_aplicatie_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.is_aplicatie_mobile.model.DetaliiLivrare
import com.example.is_aplicatie_mobile.model.Salon
import com.example.is_aplicatie_mobile.network.HospiHelpApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NurseViewModel(private val apiService: HospiHelpApiService) : ViewModel() {

    private val _saloane = MutableStateFlow<List<Salon>>(emptyList())
    val saloane = _saloane.asStateFlow()

    private val _detaliiLivrare = MutableStateFlow<List<DetaliiLivrare>>(emptyList())
    val detaliiLivrare = _detaliiLivrare.asStateFlow()

    fun loadSaloaneDinCloud(token: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getSaloane("Bearer $token")
                if (response.isSuccessful) {
                    _saloane.value = response.body() ?: emptyList()
                }
            } catch (_: Exception) { }
        }
    }

    fun loadPacientiPentruSalon(nrSalon: Int, token: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getComenziBySalon("Bearer $token", nrSalon)
                if (response.isSuccessful) {
                    val comenzi = response.body() ?: emptyList()
                    _detaliiLivrare.value = comenzi.map { comanda ->
                        DetaliiLivrare(
                            id = comanda.idComanda.toString(),
                            numePacient = "${comanda.prescriptie?.pacient?.nume ?: ""} ${comanda.prescriptie?.pacient?.prenume ?: ""}".trim(),
                            pat = "Pat ${comanda.pat?.idPat ?: "N/A"}",
                            medicament = comanda.prescriptie?.medicament?.denumire ?: "Nespecificat",
                            status = comanda.status
                        )
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // ACEASTA ESTE FUNCȚIA PENTRU ROBOT (ADMIN)
    fun loadTransportCurent(token: String) {
        viewModelScope.launch {
            try {
                // Apelăm noul endpoint din backend care aduce TOT
                val response = apiService.getTransportCurent("Bearer $token")
                if (response.isSuccessful) {
                    val date = response.body() ?: emptyList()

                    _detaliiLivrare.value = date.map { comanda ->
                        Log.d("NurseVM", "Procesez comanda ID: ${comanda.idComanda} pentru pacient: ${comanda.prescriptie?.pacient?.nume}")
                        DetaliiLivrare(
                            id = comanda.idComanda.toString(),
                            numePacient = "${comanda.prescriptie?.pacient?.nume ?: "Nume"} ${comanda.prescriptie?.pacient?.prenume ?: "Lipsa"}".trim(),
                            pat = "Salon ${comanda.pat?.nrSalon ?: "?"} - Pat ${comanda.pat?.idPat ?: "?"}",
                            medicament = comanda.prescriptie?.medicament?.denumire ?: "Nespecificat",
                            status = comanda.status
                        )
                    }
                    Log.d("NurseVM", "Transport curent încărcat: ${date.size} comenzi")
                }
            } catch (e: Exception) {
                Log.e("NurseVM", "Eroare loadTransportCurent: ${e.message}")
            }
        }
    }

    fun confirmaFinalizarePreluare(token: String, numePacientiSelectati: Set<String>) {
        viewModelScope.launch {
            val livrariSelectate = _detaliiLivrare.value.filter { it.numePacient in numePacientiSelectati }
            livrariSelectate.forEach { livrare ->
                try {
                    apiService.actualizeazaStatusComanda(
                        token = "Bearer $token",
                        idComanda = livrare.id.toInt(),
                        body = mapOf("status" to "FINALIZAT")
                    )
                } catch (e: Exception) { e.printStackTrace() }
            }
            // După confirmare, facem un refresh automat pentru a vedea statusul "LIVRAT"
            loadTransportCurent(token)
        }
    }
}