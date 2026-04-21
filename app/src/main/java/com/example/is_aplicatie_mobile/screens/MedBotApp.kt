package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun MedBotApp(onLogout: () -> Unit) {
    // Am eliminat Scaffold-ul care conținea bottomBar (bara de jos cu iconițe)
    // Administratorul va vedea direct ecranul de control, conform schiței

    Box(modifier = Modifier.fillMaxSize()) {
        AdminDashboard(onLogout = onLogout)
    }
}