package com.example.is_aplicatie_mobile.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.is_aplicatie_mobile.viewmodel.ConnectionState
import com.example.is_aplicatie_mobile.viewmodel.ModControl
import com.example.is_aplicatie_mobile.viewmodel.OperatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onLogout: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToCloudComenzi: () -> Unit,
    viewModel: OperatorViewModel,
    onNavigateToSchimbareMod: () -> Unit,
    onNavigateToTeleghidare: () -> Unit,
    onNavigateToAvarii: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val isConnected = connectionState == ConnectionState.CONNECTED
    val errorMessage by viewModel.errorMessage.collectAsState()
    val modCurent by viewModel.modCurent.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "Reîncearcă",
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.conecteazaLaWebSocket()
            }
        }
    }

    val (statusText, statusColor) = when (connectionState) {
        ConnectionState.DISCONNECTED -> "Neconectat" to Color(0xFFD32F2F)
        ConnectionState.CONNECTING -> "Se conectează..." to Color(0xFF757575)
        ConnectionState.CONNECTED -> "Conectat" to Color(0xFF2E7D32)
        ConnectionState.ERROR -> "Eroare conexiune" to Color(0xFFB71C1C)
    }

    val connectButtonText = when (connectionState) {
        ConnectionState.CONNECTING -> "Se conectează..."
        ConnectionState.CONNECTED -> "Conectat ✓"
        else -> "Conectare Robot"
    }

    val connectButtonEnabled = connectionState == ConnectionState.DISCONNECTED ||
        connectionState == ConnectionState.ERROR

    val connectDisabledColor = when (connectionState) {
        ConnectionState.CONNECTED -> Color(0xFF2E7D32)
        ConnectionState.CONNECTING -> Color.LightGray.copy(alpha = 0.7f)
        else -> Color.LightGray.copy(alpha = 0.5f)
    }

    val controlModeText = if (modCurent == ModControl.AUTOMAT) "La distanță" else "Teleghidat"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (connectionState == ConnectionState.CONNECTING) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF757575)
                    )
                }
            }

            Text(
                text = "Mod curent: $controlModeText",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            RobotMenuButton(
                text = connectButtonText,
                icon = Icons.Default.Wifi,
                isActive = connectButtonEnabled,
                color = if (connectionState == ConnectionState.CONNECTED) {
                    Color(0xFF2E7D32)
                } else {
                    Color(0xFF1976D2)
                },
                disabledContainerColor = connectDisabledColor,
                onClick = { viewModel.conecteazaLaWebSocket() }
            )

            RobotMenuButton(
                text = "Comenzi din Cloud",
                icon = Icons.Default.Cloud,
                isActive = true,
                color = Color(0xFF1976D2),
                onClick = onNavigateToCloudComenzi
            )

            RobotMenuButton(
                text = "Trimitere rapoarte despre transportul curent",
                icon = Icons.Default.Description,
                isActive = true,
                color = Color(0xFF1976D2),
                onClick = onNavigateToReports
            )

            RobotMenuButton(
                text = "Schimbare Mod",
                icon = Icons.Default.SyncAlt,
                isActive = isConnected,
                color = Color(0xFF1976D2),
                onClick = onNavigateToSchimbareMod
            )

            val isTeleghidareActive = isConnected && modCurent == ModControl.TELEGHIDARE

            RobotMenuButton(
                text = "Teleghidare",
                icon = Icons.Default.Gamepad,
                isActive = isTeleghidareActive,
                color = Color(0xFF1976D2),
                onClick = onNavigateToTeleghidare
            )

            RobotMenuButton(
                text = "Avarii",
                icon = Icons.Default.ReportProblem,
                isActive = true,
                color = Color(0xFFC62828),
                onClick = onNavigateToAvarii
            )
        }
    }
}

@Composable
fun RobotMenuButton(
    text: String,
    icon: ImageVector,
    isActive: Boolean,
    color: Color,
    disabledContainerColor: Color = Color.LightGray.copy(alpha = 0.5f),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isActive,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = disabledContainerColor,
            contentColor = Color.White,
            disabledContentColor = Color.Gray.copy(alpha = 0.8f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}
