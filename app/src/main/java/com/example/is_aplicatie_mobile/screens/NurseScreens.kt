package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.is_aplicatie_mobile.viewmodel.NurseViewModel

private val BackgroundColor = Color(0xFFF4F8FB)
private val PrimaryBlue = Color(0xFF1976D2)
private val DarkBlue = Color(0xFF0D47A1)
private val SuccessGreen = Color(0xFF4CAF50)
private val SoftBlue = Color(0xFFE3F2FD)
private val SoftBorder = Color(0xFFE0EAF5)

@Composable
fun NurseDashboard(
    viewModel: NurseViewModel,
    onSalonSelected: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val saloaneRaw by viewModel.saloane.collectAsState()

    val saloaneVizibile = remember(saloaneRaw) {
        saloaneRaw.distinctBy { it.nrSalon }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SoftBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Saloane Pacienți",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkBlue
                        )

                        Text(
                            text = "Gestionare livrări active",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    ) { padding ->

        LazyVerticalGrid(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(18.dp),
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(saloaneVizibile) { salon ->
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .height(160.dp)
                        .clickable { onSalonSelected(salon.nrSalon) },
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(SoftBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Salon ${salon.nrSalon}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkBlue
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Vezi comenzi",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WardDetailScreen(
    salonId: Int,
    token: String,
    viewModel: NurseViewModel,
    onBack: () -> Unit
) {
    val livrari by viewModel.detaliiLivrare.collectAsState()
    var pacientiSelectati by remember { mutableStateOf(setOf<String>()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(salonId) {
        pacientiSelectati = emptySet()
    }

    if (showSuccessMessage) {
        AlertDialog(
            onDismissRequest = { showSuccessMessage = false },
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            showSuccessMessage = false
                            pacientiSelectati = emptySet()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("ÎNȚELES", fontWeight = FontWeight.Bold)
                    }
                }
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(72.dp)
                )
            },
            title = {
                Text(
                    text = "Succes!",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkBlue
                )
            },
            text = {
                Text(
                    text = "Preluarea medicamentelor a fost confirmată în sistem.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Text("Saloane")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Salon $salonId",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkBlue
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        viewModel.confirmaFinalizarePreluare(token, pacientiSelectati)
                        showSuccessMessage = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = pacientiSelectati.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = "CONFIRMĂ (${pacientiSelectati.size})",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(livrari) { livrare ->

                val isSelected = pacientiSelectati.contains(livrare.numePacient)
                val isFinalizat = livrare.status == "FINALIZAT"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = if (isSelected) PrimaryBlue else SoftBorder,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable(enabled = !isFinalizat) {
                            pacientiSelectati =
                                if (isSelected) pacientiSelectati - livrare.numePacient
                                else pacientiSelectati + livrare.numePacient
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isFinalizat -> Color(0xFFE8F5E9)
                            isSelected -> SoftBlue
                            else -> Color.White
                        }
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected || isFinalizat) Color(0xFFC8E6C9)
                                    else SoftBlue
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected || isFinalizat)
                                    Icons.Default.CheckCircle
                                else
                                    Icons.Default.Medication,
                                contentDescription = null,
                                tint = if (isFinalizat) SuccessGreen else PrimaryBlue
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = livrare.numePacient,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = DarkBlue
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = livrare.pat,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )

                            Text(
                                text = "💊 ${livrare.medicament}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF455A64)
                            )

                            if (isFinalizat) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Status: Livrat",
                                    color = SuccessGreen,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}