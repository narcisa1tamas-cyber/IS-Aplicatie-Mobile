package com.example.is_aplicatie_mobile.viewmodel

import androidx.lifecycle.ViewModel
import com.example.is_aplicatie_mobile.model.DetaliiLivrare
import com.example.is_aplicatie_mobile.model.Salon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NurseViewModel : ViewModel() {
    val saloane = listOf(
        Salon(1, "Salon 1", 4),
        Salon(2, "Salon 2", 2),
        Salon(3, "Salon 3", 3),
        Salon(4, "Salon 4", 2)
    )

    private val _detaliiLivrare = MutableStateFlow<List<DetaliiLivrare>>(emptyList())
    val detaliiLivrare = _detaliiLivrare.asStateFlow()

    fun loadPacientiPentruSalon(idSalon: Int) {
        _detaliiLivrare.value = listOf(
            DetaliiLivrare("1", "Popescu Ion", "Pat A", "Aspirină", "Activ"),
            DetaliiLivrare("2", "Ionescu Maria", "Pat B", "Paracetamol", "În așteptare"),
            DetaliiLivrare("3", "Georgescu Dan", "Pat C", "Nurofen", "Activ")
        )
    }

    fun confirmaFinalizarePreluare(numePacientiSelectati: Set<String>) {
        _detaliiLivrare.value = _detaliiLivrare.value.map { livrare ->
            if (livrare.numePacient in numePacientiSelectati) {
                livrare.copy(status = "Finalizat")
            } else {
                livrare
            }
        }
    }
}