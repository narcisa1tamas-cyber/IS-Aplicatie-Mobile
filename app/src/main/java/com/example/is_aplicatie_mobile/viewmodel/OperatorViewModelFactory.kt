package com.example.is_aplicatie_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.is_aplicatie_mobile.network.HospiHelpApiService

class OperatorViewModelFactory(
    private val apiService: HospiHelpApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OperatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OperatorViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
