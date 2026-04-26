package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.is_aplicatie_mobile.viewmodel.NurseViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: NurseViewModel, onBack: () -> Unit, token: String) {
    // Colectăm starea livrărilor
    val livrari by viewModel.detaliiLivrare.collectAsState()

    // MECANISM AUTO-REFRESH:
    // Interogăm serverul la fiecare 5 secunde.
    // Dacă o comandă a fost marcată FINALIZAT în DB, la următorul loadTransportCurent
    // ea nu va mai fi returnată de server (dacă ai modificat query-ul în backend)
    LaunchedEffect(key1 = Unit) {
        while(true) {
            viewModel.loadTransportCurent(token)
            delay(5000) // Așteaptă 5 secunde până la următoarea verificare
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Livrări în Curs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (livrari.isEmpty()) {
            // Ecran când robotul a terminat toate sarcinile
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.LightGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Toate livrările au fost finalizate.\nRobotul este liber.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Sarcini active (${livrari.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                }

                // Afișăm doar comenzile care sunt în listă (cele ACTIVE)
                items(livrari) { raport ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = raport.numePacient,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = 0.5.dp,
                                color = Color.LightGray
                            )

                            Text(
                                text = "📍 Destinație: ${raport.pat}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💊 Conținut: ${raport.medicament}",
                                fontSize = 15.sp,
                                color = Color.DarkGray
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Badge pentru statusul ACTIV (singurul care va apărea aici)
                            Surface(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ÎN CURS DE LIVRARE",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = Color(0xFF1976D2),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}