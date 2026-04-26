package com.example.is_aplicatie_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.is_aplicatie_mobile.model.LoginRequest
import com.example.is_aplicatie_mobile.model.LoginResponse
import com.example.is_aplicatie_mobile.network.HospiHelpApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

class AuthViewModel(private val apiService: HospiHelpApiService) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(username: String, parola: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                // Trimitem cererea către serverul Spring Boot
                // Folosim email = username conform discuției noastre despre variabile
                val request = LoginRequest(email = username.trim(), parola = parola.trim())
                val response = apiService.login(request)

                if (response.isSuccessful && response.body() != null) {
                    _loginState.value = LoginState.Success(response.body()!!)
                } else {
                    val errorMsg = when(response.code()) {
                        401 -> "Email sau parolă incorectă!"
                        404 -> "Serverul nu a fost găsit!"
                        else -> "Eroare server: ${response.code()}"
                    }
                    _loginState.value = LoginState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Eroare de conexiune: Verificați dacă serverul și Wi-Fi-ul sunt pornite.")
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
}