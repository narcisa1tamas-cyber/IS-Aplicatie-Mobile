package com.example.is_aplicatie_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.is_aplicatie_mobile.model.LoginResponse
import kotlinx.coroutines.delay
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

class AuthViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(username: String, parola: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            // Simulăm o scurtă întârziere pentru "efectul" de încărcare de la server
            delay(1000)

            // VERIFICARE DATE HARDCODATE
            when {
                username == "asistenta" && parola == "parola123" -> {
                    _loginState.value = LoginState.Success(
                        LoginResponse(
                            id_angajat = 2,
                            nume = "Maria Ionescu",
                            rol = "Asistent",
                            token = "fake-token-asistent"
                        )
                    )
                }
                username == "admin" && parola == "admin123" -> {
                    _loginState.value = LoginState.Success(
                        LoginResponse(
                            id_angajat = 1,
                            nume = "Popescu Ion",
                            rol = "Administrator",
                            token = "fake-token-admin"
                        )
                    )
                }
                else -> {
                    _loginState.value = LoginState.Error("Date incorecte! Conturi test: asistenta/parola123 sau admin/admin123.")
                }
            }
        }
    }
}