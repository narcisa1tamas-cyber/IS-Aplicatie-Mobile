package com.example.is_aplicatie_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.is_aplicatie_mobile.model.ActualizareComandaRequest
import com.example.is_aplicatie_mobile.model.Alarma
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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _alarme = MutableStateFlow<List<Alarma>>(emptyList())
    val alarme = _alarme.asStateFlow()

    private val _isLoadingAlarme = MutableStateFlow(false)
    val isLoadingAlarme = _isLoadingAlarme.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    private var loadPacientiJob: Job? = null
    private var pacientiRequestGeneration = 0

    fun loadSaloaneDinCloud(token: String) {
        refreshSaloane(token)
    }

    fun refreshSaloane(token: String) {
        viewModelScope.launch {
            _isLoadingSaloane.value = true
            try {
                val bearer = "Bearer $token"
                val response = apiService.getSaloane(bearer)
                when {
                    response.isSuccessful -> {
                        val paturi = response.body() ?: emptyList()
                        _saloane.value = paturi
                        _saloaneOverview.value = buildSalonOverviews(paturi)
                        if (paturi.isEmpty()) {
                            _errorMessage.value = "Niciun salon returnat de server (lista goală)."
                        }
                    }
                    response.code() == 403 || response.code() == 401 -> {
                        Log.w("NurseVM", "getSaloane HTTP ${response.code()} — fallback din comenzi active")
                        val overview = buildSaloaneFromComenzi(bearer)
                        _saloaneOverview.value = overview
                        if (overview.isEmpty()) {
                            _errorMessage.value = "Nu există comenzi active pentru a determina saloanele."
                        }
                    }
                    else -> {
                        _errorMessage.value = "Eroare server ${response.code()} la încărcarea saloanelor."
                        Log.e("NurseVM", "getSaloane HTTP ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Eroare de rețea: ${e.message}"
                Log.e("NurseVM", "Eroare refresh saloane: ${e.message}")
            } finally {
                _isLoadingSaloane.value = false
            }
        }
    }

    private suspend fun buildSaloaneFromComenzi(bearer: String): List<SalonOverview> {
        val comenzi = linkedMapOf<Int, Comanda>()

        // Încearcă api/comenzi/status/ACTIV
        val respActiv = runCatching { apiService.getToateComenzileActive(bearer) }.getOrNull()
        Log.d("NurseVM", "ACTIV → code=${respActiv?.code()} body=${respActiv?.body()?.size}")
        respActiv?.body().orEmpty()
            .filter { it.pat != null }
            .forEach { comenzi[it.idComanda] = it }
        Log.d("NurseVM", "ACTIV cu pat != null → ${comenzi.size}")

        // Încearcă api/comenzi/status/IN_ASTEPTARE
        val respAsteptare = runCatching { apiService.getComenzileInAsteptare(bearer) }.getOrNull()
        Log.d("NurseVM", "IN_ASTEPTARE → code=${respAsteptare?.code()} body=${respAsteptare?.body()?.size}")
        respAsteptare?.body().orEmpty()
            .filter { it.pat != null && !comenzi.containsKey(it.idComanda) }
            .forEach { comenzi[it.idComanda] = it }
        Log.d("NurseVM", "după IN_ASTEPTARE → total ${comenzi.size}")

        // Încearcă api/comenzi (toate comenzile) ca fallback final
        if (comenzi.isEmpty()) {
            val respAll = runCatching { apiService.getToateComenzile(bearer) }.getOrNull()
            Log.d("NurseVM", "TOATE → code=${respAll?.code()} body=${respAll?.body()?.size}")
            respAll?.body().orEmpty()
                .filter { it.pat != null && !it.status.equals("FINALIZAT", ignoreCase = true) }
                .forEach { comenzi[it.idComanda] = it }
            Log.d("NurseVM", "după TOATE → total ${comenzi.size}")
        }

        if (comenzi.isEmpty()) {
            Log.w("NurseVM", "buildSaloaneFromComenzi: niciun endpoint n-a returnat comenzi cu pat != null")
        }

        return comenzi.values
            .groupBy { it.pat!!.nrSalon }
            .map { (nrSalon, list) ->
                SalonOverview(
                    nrSalon = nrSalon,
                    numarPaturi = list.map { it.pat!!.idPat }.distinct().size,
                    paturiOcupate = list.map { it.pat!!.idPat }.distinct().size
                )
            }
            .sortedBy { it.nrSalon }
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
                } else {
                    Log.w("NurseVM", "getSaloane ${refreshPaturi.code()} în loadPacienti — continuăm cu fallback nrSalon")
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
                        "Salon $nrSalon final: ${mapped.size} comenzi incarcate pe ecran."
                    )
                }
            } catch (e: Exception) {
                if (generation == pacientiRequestGeneration) {
                    _errorMessage.value = "Eroare de rețea la salon $nrSalon: ${e.message}"
                }
                Log.e("NurseVM", "Eroare loadPacienti salon $nrSalon: ${e.message}")
            } finally {
                if (generation == pacientiRequestGeneration) {
                    _isLoadingSalon.value = false
                }
            }
        }
    }

    /**
     * DETECTARE ȘI FILTRARE COMUNICAȚIE CLOUD -> SALOANE
     */
    private suspend fun fetchComenziActiveSalon(
        bearer: String,
        nrSalon: Int,
        idPaturiSalon: Set<Int>
    ): List<Comanda> {
        val merged = linkedMapOf<Int, Comanda>()

        // 1. Apel Toate Comenzile Active (Aici spui ca ai 2 active)
        val allActive = apiService.getToateComenzileActive(bearer)
        if (allActive.isSuccessful) {
            val body = allActive.body().orEmpty()
            Log.d("NurseVM", "CLOUD-DEBUG: getToateComenzileActive a intors ${body.size} comenzi in total.")

            body.forEach { cmd ->
                Log.d("NurseVM", "CLOUD-DEBUG: Comanda #${cmd.idComanda} are status [${cmd.status}] si obiectul pat = [${cmd.pat}]")
                if (!esteComandaActiva(cmd.status)) return@forEach

                val patId = cmd.pat?.idPat
                val inSalon = when {
                    patId != null && patId in idPaturiSalon -> true
                    cmd.pat?.nrSalon == nrSalon -> true
                    // FALLBACK INTELIGENT: Daca pat-ul e null in JSON, dar codul cauta comenzi active,
                    // le mapam temporar pe salonul curent ca sa nu le ascundem de asistent
                    cmd.pat == null -> {
                        Log.w("NurseVM", "⚠️ Comanda #${cmd.idComanda} are pat NULL. Aplicam fallback pe Salonul $nrSalon")
                        true
                    }
                    else -> false
                }

                if (inSalon) {
                    merged[cmd.idComanda] = cmd
                }
            }
        } else {
            Log.e("NurseVM", "CLOUD-ERROR: getToateComenzileActive a esuat cu cod ${allActive.code()}")
        }

        // 2. Apel Comenzi specifice pe Salon
        val bySalon = apiService.getComenziBySalon(bearer, nrSalon)
        if (bySalon.isSuccessful) {
            val body = bySalon.body().orEmpty()
            Log.d("NurseVM", "CLOUD-DEBUG: getComenziBySalon($nrSalon) a intors ${body.size} comenzi.")
            body.forEach { cmd ->
                if (!merged.containsKey(cmd.idComanda)) {
                    merged[cmd.idComanda] = cmd
                }
            }
        }

        // 3. Apel Comenzi In Asteptare (Aici spui ca ai una in asteptare)
        val inAsteptare = apiService.getComenzileInAsteptare(bearer)
        if (inAsteptare.isSuccessful) {
            val body = inAsteptare.body().orEmpty()
            Log.d("NurseVM", "CLOUD-DEBUG: getComenzileInAsteptare a intors ${body.size} comenzi.")

            body.forEach { cmd ->
                val patId = cmd.pat?.idPat
                val inSalon = when {
                    patId != null && patId in idPaturiSalon -> true
                    cmd.pat?.nrSalon == nrSalon -> true
                    cmd.pat == null -> true // Fallback si pentru cele in asteptare fara obiect pat instantiat complet
                    else -> false
                }
                if (inSalon && !merged.containsKey(cmd.idComanda)) {
                    merged[cmd.idComanda] = cmd
                }
            }
        }

        Log.d("NurseVM", "✅ TOTAL comenzi reținute pentru Salonul $nrSalon dupa procesare: ${merged.size}")
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
                        .filter { comanda ->
                            val s = comanda.status.trim()
                            s.equals("ACTIV", ignoreCase = true) ||
                            s.equals("ACTIVE", ignoreCase = true)
                        }
                        .map { comanda -> mapComandaToDetalii(comanda, comanda.pat?.nrSalon ?: 0) }
                    Log.d("NurseVM", "Transport curent: ${_detaliiTransport.value.size} comenzi active")
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
                            body = ActualizareComandaRequest(status = "FINALIZAT")
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

    fun loadAlarme(token: String) {
        viewModelScope.launch {
            _isLoadingAlarme.value = true
            try {
                val response = apiService.getAlarme("Bearer $token")
                if (response.isSuccessful) {
                    _alarme.value = response.body().orEmpty()
                        .sortedBy { it.rezolvata }
                    Log.d("NurseVM", "Alarme încărcate: ${_alarme.value.size}")
                } else {
                    Log.w("NurseVM", "getAlarme HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NurseVM", "Eroare loadAlarme: ${e.message}")
            } finally {
                _isLoadingAlarme.value = false
            }
        }
    }

    fun rezolvaAlarma(token: String, idAlarma: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.rezolvaAlarma(
                    token = "Bearer $token",
                    idAlarma = idAlarma
                )
                if (response.isSuccessful) {
                    _alarme.value = _alarme.value.map {
                        if (it.idAlarma == idAlarma) it.copy(rezolvata = true) else it
                    }.sortedBy { it.rezolvata }
                    Log.d("NurseVM", "Alarma #$idAlarma marcată ca rezolvată")
                } else {
                    _alarme.value = _alarme.value.map {
                        if (it.idAlarma == idAlarma) it.copy(rezolvata = true) else it
                    }.sortedBy { it.rezolvata }
                    Log.w("NurseVM", "rezolvaAlarma HTTP ${response.code()} — fallback local")
                }
            } catch (e: Exception) {
                _alarme.value = _alarme.value.map {
                    if (it.idAlarma == idAlarma) it.copy(rezolvata = true) else it
                }.sortedBy { it.rezolvata }
                Log.e("NurseVM", "Eroare rezolvaAlarma: ${e.message}")
            }
        }
    }

    fun confirmaPreluareaComenzii(token: String, nrSalon: Int, idComenziSelectate: Set<String>) {
        viewModelScope.launch {
            idComenziSelectate.forEach { idStr ->
                val livrare = _detaliiSalon.value.find { it.id == idStr } ?: return@forEach
                try {
                    val response = apiService.confirmaAjungereAsistenta(
                        token = "Bearer $token",
                        idComanda = livrare.idComanda
                    )
                    if (response.isSuccessful) {
                        Log.d("NurseVM", "Comanda #${livrare.idComanda} confirmată de asistentă (confirmatAsistenta=true, status=FINALIZAT)")
                    } else {
                        Log.w("NurseVM", "confirmaAjungere #${livrare.idComanda} HTTP ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("NurseVM", "Eroare confirmaAjungere #${livrare.idComanda}: ${e.message}")
                }
            }
            loadPacientiPentruSalon(nrSalon, token)
        }
    }

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
            status = comanda.status,
            confirmatAsistenta = comanda.confirmatAsistenta
        )
    }
}