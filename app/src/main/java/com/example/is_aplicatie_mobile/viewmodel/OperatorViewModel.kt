package com.example.is_aplicatie_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.is_aplicatie_mobile.bluetooth.RobotBluetoothManager
import kotlinx.coroutines.Dispatchers
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

class OperatorViewModel : ViewModel() {

    private val _modCurent = MutableStateFlow(ModControl.AUTOMAT)
    val modCurent = _modCurent.asStateFlow()

    private val _modVideo = MutableStateFlow(ModVideo.FARA_FLUX)
    val modVideo = _modVideo.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    val isBluetoothConnected = connectionState
        .map { it == ConnectionState.CONNECTED }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _videoStreamUrl = MutableStateFlow<String?>(null)
    val videoStreamUrl = _videoStreamUrl.asStateFlow()

    private val btManager = RobotBluetoothManager()

    fun schimbaModControl(mod: ModControl) {
        _modCurent.value = mod
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

    fun startVideoStream() {
        _videoStreamUrl.value = "http://192.168.137.1:8080/?action=stream"
    }

    fun stopVideoStream() {
        _videoStreamUrl.value = null
    }

    fun getVideoSnapshotUrl(): String = "http://192.168.137.1:8080/?action=snapshot"

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




