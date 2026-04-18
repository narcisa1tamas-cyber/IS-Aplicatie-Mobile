package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List // Importul corect pentru iconița nouă
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedBotApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Control", "Inventory", "Logs")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tabs[selectedTab], color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2)),
                navigationIcon = {
                    IconButton(onClick = { /* Meniu */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    // Status baterie robot
                    Icon(Icons.Default.BatteryFull, contentDescription = "Battery", tint = Color.Green)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("85%", color = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    // Status conexiune Bluetooth
                    Icon(Icons.Default.Bluetooth, contentDescription = "Bluetooth", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
                    label = { Text("Map") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Inventory") }, // Iconița actualizată
                    label = { Text("Inventory") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Logs") },
                    label = { Text("Logs") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> DashboardScreen()
                1 -> ControlScreen()
                2 -> Text("Ecran Inventar (În lucru)", modifier = Modifier.padding(16.dp))
                3 -> Text("Ecran Notificări (În lucru)", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

// Funcțiile placeholder definite corect la finalul fișierului
@Composable
fun DashboardScreen() {
    Text("Statistici Robot și Pacienți", modifier = Modifier.padding(16.dp))
}

@Composable
fun ControlScreen() {
    Text("Control Teleghidat Robot", modifier = Modifier.padding(16.dp))
}