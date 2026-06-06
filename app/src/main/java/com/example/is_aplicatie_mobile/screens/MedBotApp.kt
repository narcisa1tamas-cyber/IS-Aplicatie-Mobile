package com.example.is_aplicatie_mobile.screens
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.is_aplicatie_mobile.viewmodel.OperatorViewModel
// Posibil să fie nevoie să imporți și ecranele noi dacă sunt în alt pachet, dar par a fi tot în 'screens'import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

// Definim rutele posibile pentru tab-ul de Control
enum class ControlRoute {
    MENU,
    MODE_SELECTION,
    TELEOPERATION
}

@Composable
fun ControlScreen(operatorViewModel: OperatorViewModel = viewModel()) {
    var currentRoute by remember { mutableStateOf(ControlRoute.MENU) }

    when (currentRoute) {
        ControlRoute.MENU -> {
            AdminDashboard(
                viewModel = operatorViewModel,
                onLogout = { /* Logica ta de logout */ },
                onNavigateToCloudComenzi = { /* Logica comenzi cloud */ },
                onNavigateToReports = { /* Logica ta de rapoarte */ },
                onNavigateToSchimbareMod = {
                    currentRoute = ControlRoute.MODE_SELECTION
                },
                onNavigateToTeleghidare = {
                    currentRoute = ControlRoute.TELEOPERATION
                },
                onNavigateToAvarii = { /* Logica avarii */ }
            )
        }
        ControlRoute.MODE_SELECTION -> {
            ModeSelectionScreen(
                viewModel = operatorViewModel,
                onBack = { currentRoute = ControlRoute.MENU }
            )
        }
        // token și numeOperator nu sunt disponibile aici — se folosesc valorile default
        ControlRoute.TELEOPERATION -> {
            TeleoperationScreen(
                viewModel = operatorViewModel,
                onBack = { currentRoute = ControlRoute.MENU }
            )
        }
    }
}