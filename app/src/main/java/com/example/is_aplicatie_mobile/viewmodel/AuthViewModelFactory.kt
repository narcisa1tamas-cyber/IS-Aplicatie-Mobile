package com.example.is_aplicatie_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.is_aplicatie_mobile.network.HospiHelpApiService

class AuthViewModelFactory(private val apiService: HospiHelpApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}