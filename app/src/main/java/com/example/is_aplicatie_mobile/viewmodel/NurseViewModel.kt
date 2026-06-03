package com.example.is_aplicatie_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.is_aplicatie_mobile.model.Comanda
import com.example.is_aplicatie_mobile.model.DetaliiLivrare
import com.example.is_aplicatie_mobile.model.Salon
import com.example.is_aplicatie_mobile.network.HospiHelpApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SalonOverview(
    val nrSalon: Int,
    val numarPaturi: Int,
    val paturiOcupate: Int
)

class NurseViewModel(private val apiService: HospiHelpApiService) : ViewModel() {

    private val _saloane = MutableStateFlow<List<Salon>>(emptyList())
    val saloane = _saloane.asStateFlow()

    private val _saloaneOverview = MutableStateFlow<List<SalonOverview>>(emptyList())
    val saloaneOverview = _saloaneOverview.asStateFlow()

    private val _isLoadingSaloane = MutableStateFlow(false)
    val isLoadingSaloane = _isLoadingSaloane.asStateFlow()

    private val _detaliiSalon = MutableStateFlow<List<DetaliiLivrare>>(emptyList())
    val detaliiSalon = _detaliiSalon.asStateFlow()

    private val _salonCurent = MutableStateFlow<Int?>(null)
    val salonCurent = _salonCurent.asStateFlow()

    private val _isLoadingSalon = MutableStateFlow(false)
    val isLoadingSalon = _isLoadingSalon.asStateFlow()

    /** Paturi ocupate în salonul deschis (din api/paturi) — pentru comparație cu lista de comenzi. */
    private val _paturiOcupateSalonCurent = MutableStateFlow(0)
    val paturiOcupateSalonCurent = _paturiOcupateSalonCurent.asStateFlow()

    private val _detaliiTransport = MutableStateFlow<List<DetaliiLivrare>>(emptyList())
    val detaliiTransport = _detaliiTransport.asStateFlow()

    private val _toateComenzileCloud = MutableStateFlow<List<DetaliiLivrare>>(emptyList())
    val toateComenzileCloud = _toateComenzileCloud.asStateFlow()

    private val _isLoadingCloud = MutableStateFlow(false)
    val isLoadingCloud = _isLoadingCloud.asStateFlow()

    private var loadPacientiJob: Job? = null
    private var pacientiRequestGeneration = 0

    fun loadSaloaneDinCloud(token: String) {
        refreshSaloane(token)
    }

    fun refreshSaloane(token: String) {
        viewModelScope.launch {
            _isLoadingSaloane.value = true
            try {
                val response = apiService.getSaloane("Bearer $token")
                if (response.isSuccessful) {
                    val paturi = response.body() ?: emptyList()
                    _saloane.value = paturi
                    _saloaneOverview.value = buildSalonOverviews(paturi)
                }
            } catch (e: Exception) {
                Log.e("NurseVM", "Eroare refresh saloane: ${e.message}")
            } finally {
                _isLoadingSaloane.value = false
            }
        }
    }

    fun loadPacientiPentruSalon(nrSalon: Int, token: String) {
        loadPacientiJob?.cancel()
        val generation = ++pacientiRequestGeneration

        _salonCurent.value = nrSalon
        _isLoadingSalon.value = true
        _detaliiSalon.value = emptyList()

        loadPacientiJob = viewModelScope.launch {
            try {
                val bearer = "Bearer $token"
                val refreshPaturi = apiService.getSaloane(bearer)
                if (refreshPaturi.isSuccessful) {
                    val paturiNoi = refreshPaturi.body().orEmpty()
                    _saloane.value = paturiNoi
                    _saloaneOverview.value = buildSalonOverviews(paturiNoi)
                }

                val idPaturiSalon = _saloane.value
                    .filter { it.nrSalon == nrSalon }
                    .map { it.idPat }
                    .toSet()
                _paturiOcupateSalonCurent.value = _saloane.value
                    .count { it.nrSalon == nrSalon && it.ocupat }

                val comenzi = fetchComenziActiveSalon(bearer, nrSalon, idPaturiSalon)
                val mapped = comenzi
                    .distinctBy { it.idComanda }
                    .map { mapComandaToDetalii(it, nrSalon) }

                if (generation == pacientiRequestGeneration) {
                    _detaliiSalon.value = mapped
                    Log.d(
                        "NurseVM",
                        "Salon $nrSalon: ${mapped.size} comenzi active, " +
                            "${_paturiOcupateSalonCurent.value} paturi ocupate, " +
                            "paturi în salon: ${idPaturiSalon.size}"
                    )
                }
            } catch (e: Exception) {
                Log.e("NurseVM", "Eroare loadPacienti salon $nrSalon: ${e.message}")
            } finally {
                if (generation == pacientiRequestGeneration) {
                    _isLoadingSalon.value = false
                }
            }
        }
    }

    /**
     * Lista din salon = comenzile cu status ACTIV al căror pat aparține acestui salon.
     */
    private suspend fun fetchComenziActiveSalon(
        bearer: String,
        nrSalon: Int,
        idPaturiSalon: Set<Int>
    ): List<Comanda> {
        val merged = linkedMapOf<Int, Comanda>()

        val allActive = apiService.getToateComenzileActive(bearer)
        if (allActive.isSuccessful) {
            var prinPat = 0
            allActive.body().orEmpty().forEach { cmd ->
                if (!esteComandaActiva(cmd.status)) return@forEach
                val patId = cmd.pat?.idPat
                val inSalon = when {
                    patId != null && patId in idPaturiSalon -> true
                    cmd.pat?.nrSalon == nrSalon -> true
                    else -> false
                }
                if (inSalon && merged.put(cmd.idComanda, cmd) == null) prinPat++
            }
            Log.d("NurseVM", "status/ACTIV filtrat pe paturi salon $nrSalon → $prinPat, total ${merged.size}")
        }

        val bySalon = apiService.getComenziBySalon(bearer, nrSalon)
        if (bySalon.isSuccessful) {
            val body = bySalon.body().orEmpty()
            var dinEndpointSalon = 0
            body.forEach { cmd ->
                if (!merged.containsKey(cmd.idComanda)) {
                    merged[cmd.idComanda] = cmd
                    dinEndpointSalon++
                }
            }
            Log.d(
                "NurseVM",
                "salon/$nrSalon/active → ${body.size} primite, +$dinEndpointSalon noi, total ${merged.size}"
            )
        } else {
            Log.w("NurseVM", "salon/active eșuat: ${bySalon.code()}")
        }

        val inAsteptare = apiService.getComenzileInAsteptare(bearer)
        if (inAsteptare.isSuccessful) {
            var adaugate = 0
            inAsteptare.body().orEmpty().forEach { cmd ->
                val patId = cmd.pat?.idPat
                val inSalon = when {
                    patId != null && patId in idPaturiSalon -> true
                    cmd.pat?.nrSalon == nrSalon -> true
                    else -> false
                }
                if (inSalon && !merged.containsKey(cmd.idComanda)) {
                    merged[cmd.idComanda] = cmd
                    adaugate++
                }
            }
            Log.d("NurseVM", "IN_ASTEPTARE salon $nrSalon → +$adaugate, total ${merged.size}")
        } else {
            Log.w("NurseVM", "IN_ASTEPTARE eșuat: ${inAsteptare.code()}")
        }

        return merged.values.toList()
    }

    private fun esteComandaActiva(status: String): Boolean {
        val s = status.trim()
        if (s.isEmpty()) return false
        return when {
            s.equals("FINALIZAT", ignoreCase = true) -> false
            s.equals("ANULAT", ignoreCase = true) -> false
            s.equals("CANCELLED", ignoreCase = true) -> false
            else -> s.equals("ACTIV", ignoreCase = true) ||
                s.equals("ACTIVE", ignoreCase = true) ||
                s.contains("CURS", ignoreCase = true) ||
                s.contains("ASTEPTARE", ignoreCase = true)
        }
    }

    fun loadToateComenzileCloud(token: String) {
        viewModelScope.launch {
            _isLoadingCloud.value = true
            try {
                val response = apiService.getToateComenzile("Bearer $token")
                if (response.isSuccessful) {
                    val comenzi = response.body().orEmpty()
                    _toateComenzileCloud.value = comenzi
                        .distinctBy { it.idComanda }
                        .map { mapComandaToDetalii(it, it.pat?.nrSalon ?: 0) }
                    Log.d("NurseVM", "Cloud: ${_toateComenzileCloud.value.size} comenzi totale")
                } else {
                    Log.w("NurseVM", "Cloud comenzi eroare: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NurseVM", "Eroare loadToateComenzileCloud: ${e.message}")
            } finally {
                _isLoadingCloud.value = false
            }
        }
    }

    fun loadTransportCurent(token: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getToateComenzileActive("Bearer $token")
                if (response.isSuccessful) {
                    val date = response.body().orEmpty()
                    _detaliiTransport.value = date
                        .filter { it.status.equals("ACTIV", ignoreCase = true) || it.status.equals("ACTIVE", ignoreCase = true) }
                        .map { comanda -> mapComandaToDetalii(comanda, comanda.pat?.nrSalon ?: 0) }
                }
            } catch (e: Exception) {
                Log.e("NurseVM", "Eroare loadTransportCurent: ${e.message}")
            }
        }
    }

    fun confirmaFinalizarePreluare(token: String, nrSalon: Int, idComenziSelectate: Set<String>) {
        viewModelScope.launch {
            _detaliiSalon.value
                .filter { it.id in idComenziSelectate }
                .forEach { livrare ->
                    try {
                        apiService.actualizeazaStatusComanda(
                            token = "Bearer $token",
                            idComanda = livrare.idComanda,
                            body = mapOf("status" to "FINALIZAT")
                        )
                    } catch (e: Exception) {
                        Log.e("NurseVM", "Eroare confirmare ${livrare.id}: ${e.message}")
                    }
                }
            loadPacientiPentruSalon(nrSalon, token)
        }
    }

    fun clearSalonDetail() {
        loadPacientiJob?.cancel()
        pacientiRequestGeneration++
        _salonCurent.value = null
        _detaliiSalon.value = emptyList()
        _paturiOcupateSalonCurent.value = 0
        _isLoadingSalon.value = false
    }

    private fun buildSalonOverviews(paturi: List<Salon>): List<SalonOverview> =
        paturi
            .groupBy { it.nrSalon }
            .map { (nrSalon, lista) ->
                SalonOverview(
                    nrSalon = nrSalon,
                    numarPaturi = lista.size,
                    paturiOcupate = lista.count { it.ocupat }
                )
            }
            .sortedBy { it.nrSalon }

    /** Folosim salonul cerut la încărcare, nu pat.nrSalon din JSON (poate fi învechit). */
    private fun mapComandaToDetalii(comanda: Comanda, nrSalonAfisat: Int): DetaliiLivrare {
        val idPat = comanda.pat?.idPat
        return DetaliiLivrare(
            id = comanda.idComanda.toString(),
            idComanda = comanda.idComanda,
            idPat = idPat,
            nrSalon = nrSalonAfisat,
            numePacient = "${comanda.prescriptie?.pacient?.nume ?: ""} ${comanda.prescriptie?.pacient?.prenume ?: ""}".trim()
                .ifBlank { "Pacient necunoscut" },
            pat = "Pat ${idPat ?: "N/A"}",
            medicament = comanda.prescriptie?.medicament?.denumire ?: "Nespecificat",
            status = comanda.status
        )
    }
}
