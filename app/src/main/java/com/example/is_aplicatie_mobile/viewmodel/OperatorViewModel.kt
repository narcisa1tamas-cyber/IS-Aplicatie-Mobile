package com.example.is_aplicatie_mobile.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ModControl {
    AUTOMAT, // "Control de la distanta" (din cloud)
    TELEGHIDARE
}

enum class ModVideo {
    CU_FLUX,
    FARA_FLUX
}

class OperatorViewModel : ViewModel() {
    private val _modCurent = MutableStateFlow(ModControl.AUTOMAT)
    val modCurent = _modCurent.asStateFlow()

    private val _modVideo = MutableStateFlow(ModVideo.FARA_FLUX)
    val modVideo = _modVideo.asStateFlow()

    fun schimbaModControl(mod: ModControl) {
        _modCurent.value = mod
    }

    fun schimbaModVideo(mod: ModVideo) {
        _modVideo.value = mod
    }

    // Aici vei adăuga pe viitor logica de trimitere comenzi Bluetooth/Wi-Fi
    fun trimiteComandaDirectie(directie: String) {
        println("Comandă direcție trimisă: $directie")
    }
}