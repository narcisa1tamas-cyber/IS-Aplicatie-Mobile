package com.example.is_aplicatie_mobile.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.is_aplicatie_mobile.bluetooth.RobotBluetoothManager
import com.example.is_aplicatie_mobile.model.CreeazaAlarmaRequest
import com.example.is_aplicatie_mobile.model.TeleghidareOperatorRequest
import com.example.is_aplicatie_mobile.network.HospiHelpApiService
import com.example.is_aplicatie_mobile.streaming.MjpegStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class ModControl { AUTOMAT, TELEGHIDARE }
enum class ModVideo { CU_FLUX, FARA_FLUX }

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

class OperatorViewModel(private val apiService: HospiHelpApiService) : ViewModel() {

    companion object {
        const val VIDEO_STREAM_URL   = "https://both-stardust-unhidden.ngrok-free.dev/video"
        const val VIDEO_SNAPSHOT_URL = "https://both-stardust-unhidden.ngrok-free.dev/video"
    }

    private val _modCurent = MutableStateFlow(ModControl.AUTOMAT)
    val modCurent = _modCurent.asStateFlow()

    private val _modVideo = MutableStateFlow(ModVideo.FARA_FLUX)
    val modVideo = _modVideo.asStateFlow()

    private val _operatorTeleghidare = MutableStateFlow<String?>(null)
    val operatorTeleghidare = _operatorTeleghidare.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    val isBluetoothConnected = connectionState
        .map { it == ConnectionState.CONNECTED }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _videoStreamUrl = MutableStateFlow<String?>(null)
    val videoStreamUrl = _videoStreamUrl.asStateFlow()

    private val _currentFrame = MutableStateFlow<ImageBitmap?>(null)
    val currentFrame = _currentFrame.asStateFlow()

    private val _videoLoading = MutableStateFlow(false)
    val videoLoading = _videoLoading.asStateFlow()

    private var streamJob: Job? = null
    private var btReadJob: Job? = null
    private var sessionToken: String = ""

    private val btManager = RobotBluetoothManager()

    fun schimbaModControl(mod: ModControl) {
        _modCurent.value = mod
    }

    fun schimbaModControlCuOperator(mod: ModControl, token: String, numeOperator: String) {
        _modCurent.value = mod
        if (mod == ModControl.TELEGHIDARE) {
            _operatorTeleghidare.value = numeOperator
            transmiteOperatorCatreCLoud(token, numeOperator)
        } else {
            _operatorTeleghidare.value = null
        }
    }

    private fun transmiteOperatorCatreCLoud(token: String, numeOperator: String) {
        viewModelScope.launch {
            try {
                val response = apiService.transmiteOperatorTeleghidare(
                    token = "Bearer $token",
                    body = TeleghidareOperatorRequest(numeOperator = numeOperator)
                )
                if (response.isSuccessful) {
                    Log.d("OperatorVM", "Operator teleghidare transmis la cloud: $numeOperator")
                } else {
                    Log.w("OperatorVM", "transmiteOperator HTTP ${response.code()} — operatorul rămâne setat local")
                }
            } catch (e: Exception) {
                Log.e("OperatorVM", "Eroare transmitere operator la cloud: ${e.message}")
            }
        }
    }

    fun schimbaModVideo(mod: ModVideo) {
        _modVideo.value = mod
    }

    fun conecteazaLaDispozitivDisponibil() {
        if (_connectionState.value == ConnectionState.CONNECTED ||
            _connectionState.value == ConnectionState.CONNECTING
        ) return
        conecteazaLaWebSocket()
    }

    fun conecteazaLaWebSocket() {
        if (_connectionState.value == ConnectionState.CONNECTED ||
            _connectionState.value == ConnectionState.CONNECTING
        ) return

        _errorMessage.value = null
        _connectionState.value = ConnectionState.CONNECTING

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("BT", "Se încearcă conectarea Bluetooth la robot...")
                val succes = btManager.conecteaza()
                if (succes) {
                    _connectionState.value = ConnectionState.CONNECTED
                    _errorMessage.value = null
                    Log.d("BT", "Conexiune Bluetooth realizată cu succes!")
                    pornesteCitireBluetooth()
                } else {
                    _connectionState.value = ConnectionState.ERROR
                    _errorMessage.value = btManager.lastError.ifEmpty {
                        "Nu s-a găsit robotul. Verifică că Bluetooth-ul este activ și robotul este împerecheat."
                    }
                    Log.e("BT", "Conectare Bluetooth eșuată: ${btManager.lastError}")
                }
            } catch (e: Exception) {
                Log.e("BT", "Eroare fatală Bluetooth: ${e.message}")
                _connectionState.value = ConnectionState.ERROR
                _errorMessage.value = "Eroare Bluetooth: ${e.localizedMessage ?: "Conexiune imposibilă."}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /** Stochează token-ul de sesiune necesar pentru a trimite avarii la cloud. */
    fun setSessionToken(token: String) {
        sessionToken = token
    }

    /**
     * Pornește o corutină IO care citește continuu mesajele JSON trimise de robot.
     * Dacă un mesaj conține câmpul "avarie": true, îl raportează automat la cloud.
     * Format așteptat de la robot:
     *   {"avarie": true, "tip": "SENZOR_DISTANTA", "descriere": "Senzor față defect"}
     */
    private fun pornesteCitireBluetooth() {
        btReadJob?.cancel()
        btReadJob = viewModelScope.launch(Dispatchers.IO) {
            btManager.citesteFlux { linie ->
                try {
                    val json = org.json.JSONObject(linie)
                    if (json.optBoolean("avarie", false)) {
                        val tip = json.optString("tip", "NECUNOSCUT")
                        val mesaj = json.optString("mesaj", json.optString("descriere", ""))
                        val idComanda = if (json.has("idComanda")) json.optInt("idComanda") else null
                        Log.w("BT-ALARMA", "Alarma detectată: $tip — $mesaj (comanda=$idComanda)")
                        raporteazaAvarieLaCloud(tip, mesaj, idComanda)
                    }
                } catch (e: Exception) {
                    Log.d("BT-IN", "Mesaj non-JSON ignorat: $linie")
                }
            }
        }
    }

    private fun raporteazaAvarieLaCloud(tip: String, mesaj: String, idComanda: Int? = null) {
        if (sessionToken.isBlank()) {
            Log.w("OperatorVM", "Token lipsă — alarma nu a putut fi trimisă la cloud")
            return
        }
        viewModelScope.launch {
            try {
                val response = apiService.creeazaAlarma(
                    token = "Bearer $sessionToken",
                    body = CreeazaAlarmaRequest(tip = tip, mesaj = mesaj, idComanda = idComanda)
                )
                if (response.isSuccessful) {
                    Log.d("OperatorVM", "Alarma [$tip] raportată la cloud (id=${response.body()?.idAlarma})")
                } else {
                    Log.w("OperatorVM", "Creare alarma HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("OperatorVM", "Eroare trimitere alarma la cloud: ${e.message}")
            }
        }
    }

    fun startVideoStream() {
        if (streamJob?.isActive == true) return
        _videoStreamUrl.value = VIDEO_STREAM_URL
        _videoLoading.value = true
        _currentFrame.value = null

        streamJob = viewModelScope.launch {
            try {
                MjpegStreamReader.streamFrames(VIDEO_STREAM_URL)
                    .collect { frame ->
                        _videoLoading.value = false
                        _currentFrame.value = frame
                    }
            } catch (e: Exception) {
                Log.e("OperatorVM", "Stream MJPEG oprit: ${e.message}")
                _videoLoading.value = false
            }
        }
    }

    fun stopVideoStream() {
        streamJob?.cancel()
        streamJob = null
        _videoStreamUrl.value = null
        _currentFrame.value = null
        _videoLoading.value = false
    }

    fun restartVideoStream() {
        stopVideoStream()
        startVideoStream()
    }

    fun getVideoSnapshotUrl(): String = VIDEO_SNAPSHOT_URL

    fun trimiteComandaDirectie(directie: String) {
        if (_connectionState.value != ConnectionState.CONNECTED) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("actiune", "press")
                    put("directie", directie.uppercase())
                }
                btManager.trimiteMesaj(json.toString())
                Log.d("BT", "Trimis Press: $directie")
            } catch (e: Exception) {
                Log.e("BT", "Eroare transmisie: ${e.message}")
            }
        }
    }

    fun trimiteStop() {
        if (_connectionState.value != ConnectionState.CONNECTED) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("actiune", "release")
                    put("directie", "STOP")
                }
                btManager.trimiteMesaj(json.toString())
                Log.d("BT", "Trimis Release Stop")
            } catch (e: Exception) {
                Log.e("BT", "Eroare transmisie stop: ${e.message}")
            }
        }
    }

    fun inchideConexiune() {
        btReadJob?.cancel()
        btReadJob = null
        viewModelScope.launch(Dispatchers.IO) {
            btManager.deconecteaza()
        }
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override fun onCleared() {
        super.onCleared()
        inchideConexiune()
    }
}




