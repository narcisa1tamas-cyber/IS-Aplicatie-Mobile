package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.is_aplicatie_mobile.screens.AdminDashboard // Importăm dashboard-ul creat anterior

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedBotApp(onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Control", "Map", "Inventory", "Logs")

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Build, contentDescription = "Control") },
                    label = { Text("Control") },
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
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Inventory") },
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
                // Aici chemăm AdminDashboard-ul cu butoanele de Bluetooth, Cloud, etc.
                0 -> AdminDashboard(onLogout = onLogout)

                1 -> Text("Ecran Hartă (În lucru)", modifier = Modifier.padding(16.dp))
                2 -> Text("Ecran Inventar (În lucru)", modifier = Modifier.padding(16.dp))
                3 -> Text("Ecran Log-uri (În lucru)", modifier = Modifier.padding(16.dp))
            }
        }
    }
}