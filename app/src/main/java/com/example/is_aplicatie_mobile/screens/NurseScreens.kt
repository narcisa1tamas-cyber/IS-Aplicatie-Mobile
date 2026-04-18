package com.example.is_aplicatie_mobile.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.is_aplicatie_mobile.viewmodel.NurseViewModel

@Composable
fun NurseDashboard(viewModel: NurseViewModel, onSalonSelected: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Saloane Pacienți",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(viewModel.saloane) { salon ->
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onSalonSelected(salon.id) },
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MeetingRoom, null, tint = Color(0xFF1976D2), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(salon.nume, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WardDetailScreen(salonId: Int, viewModel: NurseViewModel, onBack: () -> Unit) {
    val livrari by viewModel.detaliiLivrare.collectAsState()
    var pacientiSelectati by remember { mutableStateOf(setOf<String>()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(salonId) {
        viewModel.loadPacientiPentruSalon(salonId)
        pacientiSelectati = emptySet()
    }

    if (showSuccessMessage) {
        AlertDialog(
            onDismissRequest = { showSuccessMessage = false },
            confirmButton = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(onClick = { showSuccessMessage = false; pacientiSelectati = emptySet() }) {
                        Text("ÎNȚELES")
                    }
                }
            },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(70.dp)) },
            title = { Text("Succes!", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = { Text("Preluarea medicamentelor a fost confirmată.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
        )
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        Text("Saloane")
                    }
                    Spacer(Modifier.weight(1f))
                    Text("Salon $salonId", fontWeight = FontWeight.Black)
                }
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.confirmaFinalizarePreluare(pacientiSelectati)
                    showSuccessMessage = true
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                enabled = pacientiSelectati.isNotEmpty()
            ) {
                Text("CONFIRMĂ (${pacientiSelectati.size})")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(livrari) { livrare ->
                val isSelected = pacientiSelectati.contains(livrare.numePacient)
                val isFinalizat = livrare.status == "Finalizat"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable(enabled = !isFinalizat) {
                            pacientiSelectati = if (isSelected) pacientiSelectati - livrare.numePacient else pacientiSelectati + livrare.numePacient
                        }
                        .border(2.dp, if (isSelected) Color(0xFF1976D2) else Color.Transparent, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(livrare.numePacient, fontWeight = FontWeight.Bold)
                            Text("Pat: ${livrare.pat} • 💊 ${livrare.medicament}", color = Color.Gray)
                        }
                        if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF1976D2))
                    }
                }
            }
        }
    }
}