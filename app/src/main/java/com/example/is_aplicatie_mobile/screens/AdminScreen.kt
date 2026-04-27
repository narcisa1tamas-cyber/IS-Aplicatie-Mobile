package com.example.is_aplicatie_mobile.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// IMPORTURILE NOI
import com.example.is_aplicatie_mobile.viewmodel.ModControl
import com.example.is_aplicatie_mobile.viewmodel.OperatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onLogout: () -> Unit,
    onNavigateToReports: () -> Unit,
    // AM ADĂUGAT PARAMETRII NOI PENTRU NAVIGARE ȘI VIEWMODEL
    viewModel: OperatorViewModel,
    onNavigateToSchimbareMod: () -> Unit,
    onNavigateToTeleghidare: () -> Unit
) {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(false) }

    // CITIM MODUL DIRECT DIN VIEWMODEL ÎN LOC SĂ FIE VARIABILĂ LOCALĂ
    val modCurent by viewModel.modCurent.collectAsState()
    val controlModeText = if (modCurent == ModControl.AUTOMAT) "La distanță" else "Teleghidat"

    var alertMessage by remember { mutableStateOf("") }
    var showSimpleAlert by remember { mutableStateOf(false) }
    var showBluetoothDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meniu comenzi robot", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isConnected) "Conectat" else "Neconectat",
                color = if (isConnected) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Mod curent: $controlModeText",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            RobotMenuButton(
                text = "Conectare Robot",
                icon = Icons.Default.Bluetooth,
                isActive = true,
                color = if (isConnected) Color(0xFF4CAF50) else Color(0xFF1976D2),
                onClick = {
                    if (!isConnected) showBluetoothDialog = true
                    else { alertMessage = "Robot conectat"; showSimpleAlert = true }
                }
            )

            RobotMenuButton(
                text = "Comenzi din Cloud",
                icon = Icons.Default.Cloud,
                isActive = isConnected,
                onClick = {
                    if(!isConnected) { alertMessage = "Robot neconectat!"; showSimpleAlert = true }
                }
            )

            RobotMenuButton(
                text = "Trimitere rapoarte despre transportul curent",
                icon = Icons.Default.Description,
                isActive = isConnected,
                onClick = {
                    if (!isConnected) {
                        alertMessage = "Robot neconectat!"
                        showSimpleAlert = true
                    } else {
                        onNavigateToReports()
                    }
                }
            )

            // MODIFICAT AICI: Deschide ecranul de selectare a modului
            RobotMenuButton(
                text = "Schimbare Mod",
                icon = Icons.Default.SyncAlt,
                isActive = isConnected,
                onClick = {
                    if(!isConnected) {
                        alertMessage = "Robot neconectat!"; showSimpleAlert = true
                    } else {
                        onNavigateToSchimbareMod()
                    }
                }
            )

            val isTeleghidareActive = isConnected && modCurent == ModControl.TELEGHIDARE

            // MODIFICAT AICI: Deschide ecranul de teleghidare cu Joystick
            RobotMenuButton(
                text = "Teleghidare",
                icon = Icons.Default.Gamepad,
                isActive = isTeleghidareActive,
                onClick = {
                    if (!isConnected) {
                        alertMessage = "Robot neconectat!"
                        showSimpleAlert = true
                    } else if (modCurent != ModControl.TELEGHIDARE) {
                        alertMessage = "Disponibil doar în mod teleghidare"
                        showSimpleAlert = true
                    } else {
                        onNavigateToTeleghidare()
                    }
                }
            )

            RobotMenuButton(
                text = "Avarii",
                icon = Icons.Default.ReportProblem,
                isActive = isConnected,
                color = Color(0xFFC62828),
                onClick = {
                    if(!isConnected) { alertMessage = "Robot neconectat!"; showSimpleAlert = true }
                }
            )
        }
    }

    if (showSimpleAlert) {
        AlertDialog(
            onDismissRequest = { showSimpleAlert = false },
            confirmButton = { TextButton(onClick = { showSimpleAlert = false }) { Text("OK") } },
            text = { Text(alertMessage, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
        )
    }

    if (showBluetoothDialog) {
        AlertDialog(
            onDismissRequest = { showBluetoothDialog = false },
            title = { Text("Bluetooth dezactivat", fontWeight = FontWeight.Bold) },
            text = { Text("Va rugam activati/deschideti bluetooth") },
            confirmButton = {
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                        showBluetoothDialog = false
                        isConnected = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) { Text("Open Bluetooth") }
            }
        )
    }
}

@Composable
fun RobotMenuButton(
    text: String,
    icon: ImageVector,
    isActive: Boolean,
    color: Color = Color(0xFF1976D2),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) color else Color.LightGray.copy(alpha = 0.6f),
            contentColor = if (isActive) Color.White else Color.Gray
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            )
        }
    }
}