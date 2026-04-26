package com.example.is_aplicatie_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.is_aplicatie_mobile.network.RetrofitClient
import com.example.is_aplicatie_mobile.screens.*
import com.example.is_aplicatie_mobile.ui.theme.ISAplicatieMobileTheme
import com.example.is_aplicatie_mobile.viewmodel.AuthViewModel
import com.example.is_aplicatie_mobile.viewmodel.AuthViewModelFactory
import com.example.is_aplicatie_mobile.viewmodel.NurseViewModel
import com.example.is_aplicatie_mobile.viewmodel.NurseViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ISAplicatieMobileTheme {
                // State-uri pentru navigare și sesiune
                var currentScreen by remember { mutableStateOf("splash") }
                var userRole by remember { mutableStateOf("") }
                var userToken by remember { mutableStateOf("") }
                var selectedSalonId by remember { mutableIntStateOf(-1) }

                // Initializare API Service
                val apiService = RetrofitClient.instance

                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(apiService)
                )

                val nurseViewModel: NurseViewModel = viewModel(
                    factory = NurseViewModelFactory(apiService)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFF1F5F9), Color(0xFFFFFFFF))
                            )
                        )
                ) {
                    Scaffold(
                        containerColor = Color.Transparent,
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                "splash" -> SplashScreen(
                                    onContinue = { currentScreen = "login" }
                                )

                                "login" -> {
                                    BackHandler { currentScreen = "splash" }
                                    LoginScreen(
                                        onLoginSuccess = { user ->
                                            userRole = user.rol
                                            userToken = user.token

                                            if (userRole == "ASISTENTA") {
                                                nurseViewModel.loadSaloaneDinCloud(userToken)
                                                currentScreen = "nurse_dashboard"
                                            } else {
                                                // Rol ADMIN / CONTROL ROBOT
                                                currentScreen = "main_app"
                                            }
                                        },
                                        authViewModel = authViewModel
                                    )
                                }

                                "nurse_dashboard" -> {
                                    BackHandler {
                                        authViewModel.resetLoginState()
                                        currentScreen = "login"
                                    }
                                    NurseDashboard(
                                        viewModel = nurseViewModel,
                                        onSalonSelected = { id ->
                                            selectedSalonId = id
                                            nurseViewModel.loadPacientiPentruSalon(id, userToken)
                                            currentScreen = "ward_details"
                                        },
                                        onLogout = {
                                            authViewModel.resetLoginState()
                                            currentScreen = "login"
                                        }
                                    )
                                }

                                "ward_details" -> {
                                    BackHandler { currentScreen = "nurse_dashboard" }
                                    WardDetailScreen(
                                        salonId = selectedSalonId,
                                        token = userToken,
                                        viewModel = nurseViewModel,
                                        onBack = { currentScreen = "nurse_dashboard" }
                                    )
                                }

                                "main_app" -> {
                                    BackHandler {
                                        authViewModel.resetLoginState()
                                        currentScreen = "login"
                                    }
                                    // Aici afișăm AdminDashboard (Control Robot)
                                    AdminDashboard(
                                        onLogout = {
                                            authViewModel.resetLoginState()
                                            currentScreen = "login"
                                        },
                                        onNavigateToReports = {
                                            nurseViewModel.loadTransportCurent(userToken) // ACEASTA aduce toți pacienții
                                            currentScreen = "admin_reports"
                                        }
                                    )
                                }

                                "admin_reports" -> {
                                    // Butonul "Back" de pe telefon te duce înapoi la meniul robotului
                                    BackHandler { currentScreen = "main_app" }

                                    ReportsScreen(
                                        viewModel = nurseViewModel,
                                        onBack = { currentScreen = "main_app" },
                                        token = userToken
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}