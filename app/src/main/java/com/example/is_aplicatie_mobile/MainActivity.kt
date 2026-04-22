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
import com.example.is_aplicatie_mobile.screens.*
import com.example.is_aplicatie_mobile.ui.theme.ISAplicatieMobileTheme
import com.example.is_aplicatie_mobile.viewmodel.AuthViewModel
import com.example.is_aplicatie_mobile.viewmodel.NurseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ISAplicatieMobileTheme {
                var currentScreen by remember { mutableStateOf("splash") }
                var userRole by remember { mutableStateOf("") }
                var selectedSalonId by remember { mutableIntStateOf(-1) }

                val authViewModel: AuthViewModel = viewModel()
                val nurseViewModel: NurseViewModel = viewModel()

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
                                            currentScreen =
                                                if (userRole == "Asistent") "nurse_dashboard"
                                                else "main_app"
                                        },
                                        authViewModel = authViewModel
                                    )
                                }

                                "nurse_dashboard" -> {
                                    BackHandler {
                                        authViewModel.resetLoginState()
                                        userRole = ""
                                        selectedSalonId = -1
                                        currentScreen = "login"
                                    }

                                    NurseDashboard(
                                        viewModel = nurseViewModel,
                                        onSalonSelected = { id ->
                                            selectedSalonId = id
                                            currentScreen = "ward_details"
                                        },
                                        onLogout = {
                                            authViewModel.resetLoginState()
                                            userRole = ""
                                            selectedSalonId = -1
                                            currentScreen = "login"
                                        }
                                    )
                                }

                                "ward_details" -> {
                                    BackHandler { currentScreen = "nurse_dashboard" }

                                    WardDetailScreen(
                                        salonId = selectedSalonId,
                                        viewModel = nurseViewModel,
                                        onBack = { currentScreen = "nurse_dashboard" }
                                    )
                                }


                                "main_app" -> {
                                    BackHandler {
                                        authViewModel.resetLoginState()
                                        userRole = ""
                                        currentScreen = "login"
                                    }
                                    MedBotApp(
                                        onLogout = {
                                            authViewModel.resetLoginState()
                                            userRole = ""
                                            currentScreen = "login"
                                        } ,
                                        onNavigateToReports = { currentScreen = "reports_page" }
                                    )
                                }
                                "reports_page" -> {
                                    BackHandler { currentScreen = "main_app" }
                                    ReportsScreen(onBack = { currentScreen = "main_app" })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}