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
import com.example.is_aplicatie_mobile.session.SessionManager
import com.example.is_aplicatie_mobile.ui.theme.ISAplicatieMobileTheme
import com.example.is_aplicatie_mobile.viewmodel.AuthViewModel
import com.example.is_aplicatie_mobile.viewmodel.AuthViewModelFactory
import com.example.is_aplicatie_mobile.viewmodel.NurseViewModel
import com.example.is_aplicatie_mobile.viewmodel.NurseViewModelFactory
import com.example.is_aplicatie_mobile.viewmodel.OperatorViewModel
import com.example.is_aplicatie_mobile.viewmodel.OperatorViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SessionManager.init(this)

        enableEdgeToEdge()

        setContent {
            ISAplicatieMobileTheme {

                val sesiuneSalvata = SessionManager.esteLogat()
                val ecranInitial = when {
                    sesiuneSalvata && SessionManager.getRol().uppercase() == "ASISTENTA" -> "nurse_dashboard"
                    sesiuneSalvata -> "main_app"
                    else -> "splash"
                }

                var currentScreen by remember { mutableStateOf(ecranInitial) }
                var userRole    by remember { mutableStateOf(SessionManager.getRol()) }
                var userToken   by remember { mutableStateOf(SessionManager.getToken()) }
                var userNume    by remember { mutableStateOf(SessionManager.getNume()) }
                var userPrenume by remember { mutableStateOf(SessionManager.getPrenume()) }
                var selectedSalonId by remember { mutableIntStateOf(-1) }

                // Inițializare API Service
                val apiService = RetrofitClient.instance

                val sessionExpirata by SessionManager.sessionExpirata.collectAsState()
                LaunchedEffect(sessionExpirata) {
                    if (sessionExpirata) {
                        userToken   = ""
                        userRole    = ""
                        userNume    = ""
                        userPrenume = ""
                        currentScreen = "login"
                    }
                }

                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(apiService)
                )

                val nurseViewModel: NurseViewModel = viewModel(
                    factory = NurseViewModelFactory(apiService)
                )

                val operatorViewModel: OperatorViewModel = viewModel(
                    factory = OperatorViewModelFactory(apiService)
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
                                            userRole    = user.rol
                                            userToken   = user.token
                                            userNume    = user.nume
                                            userPrenume = user.prenume

                                            // Salvează sesiunea local
                                            SessionManager.salveazaSesiune(
                                                token   = user.token,
                                                rol     = user.rol,
                                                nume    = user.nume,
                                                prenume = user.prenume
                                            )

                                            if (userRole.uppercase() == "ASISTENTA") {
                                                nurseViewModel.loadSaloaneDinCloud(userToken)
                                                currentScreen = "nurse_dashboard"
                                            } else {
                                                currentScreen = "main_app"
                                            }
                                        },
                                        authViewModel = authViewModel
                                    )
                                }

                                "nurse_dashboard" -> {
                                    BackHandler {
                                        // Înapoi din dashboard minimizează aplicația, nu deloghează
                                    }
                                    LaunchedEffect(Unit) {
                                        if (userToken.isNotBlank()) {
                                            nurseViewModel.loadSaloaneDinCloud(userToken)
                                        }
                                    }

                                    NurseDashboard(
                                        viewModel = nurseViewModel,
                                        token = userToken,
                                        onSalonSelected = { id ->
                                            selectedSalonId = id
                                            currentScreen = "ward_details"
                                        },
                                        onLogout = {
                                            SessionManager.stergeSesiune()
                                            authViewModel.resetLoginState()
                                            currentScreen = "login"
                                        }
                                    )
                                }

                                "ward_details" -> {
                                    BackHandler {
                                        nurseViewModel.clearSalonDetail()
                                        nurseViewModel.refreshSaloane(userToken)
                                        currentScreen = "nurse_dashboard"
                                    }
                                    WardDetailScreen(
                                        salonId = selectedSalonId,
                                        token = userToken,
                                        viewModel = nurseViewModel,
                                        onBack = {
                                            nurseViewModel.clearSalonDetail()
                                            nurseViewModel.refreshSaloane(userToken)
                                            currentScreen = "nurse_dashboard"
                                        }
                                    )
                                }

                                "main_app" -> {
                                    BackHandler {
                                        // Înapoi din meniu principal minimizează aplicația, nu deloghează
                                    }

                                    // Încearcă conectarea automată silențioasă prin WebSocket la pornire
                                    LaunchedEffect(Unit) {
                                        operatorViewModel.setSessionToken(userToken)
                                        operatorViewModel.conecteazaLaDispozitivDisponibil()
                                    }

                                    AdminDashboard(
                                        viewModel = operatorViewModel,
                                        onLogout = {
                                            SessionManager.stergeSesiune()
                                            operatorViewModel.inchideConexiune()
                                            authViewModel.resetLoginState()
                                            currentScreen = "login"
                                        },
                                        onNavigateToCloudComenzi = {
                                            nurseViewModel.loadToateComenzileCloud(userToken)
                                            currentScreen = "cloud_comenzi"
                                        },
                                        onNavigateToReports = {
                                            nurseViewModel.loadTransportCurent(userToken)
                                            currentScreen = "admin_reports"
                                        },
                                        onNavigateToSchimbareMod = {
                                            currentScreen = "mode_selection"
                                        },
                                        onNavigateToTeleghidare = {
                                            currentScreen = "teleoperation"
                                        },
                                        onNavigateToAvarii = {
                                            nurseViewModel.loadAlarme(userToken)
                                            currentScreen = "avarii"
                                        }
                                    )
                                }

                                "cloud_comenzi" -> {
                                    BackHandler { currentScreen = "main_app" }
                                    CloudComenziScreen(
                                        viewModel = nurseViewModel,
                                        onBack = { currentScreen = "main_app" },
                                        token = userToken
                                    )
                                }

                                "admin_reports" -> {
                                    BackHandler { currentScreen = "main_app" }
                                    ReportsScreen(
                                        viewModel = nurseViewModel,
                                        onBack = { currentScreen = "main_app" },
                                        token = userToken
                                    )
                                }

                                "mode_selection" -> {
                                    BackHandler { currentScreen = "main_app" }
                                    ModeSelectionScreen(
                                        viewModel = operatorViewModel,
                                        onBack = { currentScreen = "main_app" },
                                        token = userToken,
                                        numeOperator = "$userNume $userPrenume".trim()
                                    )
                                }

                                "teleoperation" -> {
                                    BackHandler { currentScreen = "main_app" }
                                    TeleoperationScreen(
                                        viewModel = operatorViewModel,
                                        onBack = { currentScreen = "main_app" }
                                    )
                                }

                                "avarii" -> {
                                    BackHandler { currentScreen = "main_app" }
                                    AvariiScreen(
                                        viewModel = nurseViewModel,
                                        token = userToken,
                                        onBack = { currentScreen = "main_app" }
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