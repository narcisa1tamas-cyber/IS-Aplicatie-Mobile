package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun MedBotApp(
    onLogout: () -> Unit,
    onNavigateToReports: () -> Unit // Adăugat pentru a trimite mai departe funcția
) {
    // Administratorul vede direct ecranul de control
    Box(modifier = Modifier.fillMaxSize()) {
        AdminDashboard(
            onLogout = onLogout,
            onNavigateToReports = onNavigateToReports // Pasăm funcția către Dashboard
        )
    }
}