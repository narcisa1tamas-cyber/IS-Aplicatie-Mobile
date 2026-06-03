package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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
    val livrari by viewModel.detaliiTransport.collectAsState()

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudComenziScreen(viewModel: NurseViewModel, onBack: () -> Unit, token: String) {
    val toateComenzile by viewModel.toateComenzileCloud.collectAsState()
    val isLoading by viewModel.isLoadingCloud.collectAsState()

    var filtruSelectat by remember { mutableStateOf("TOATE") }

    val filtre = listOf("TOATE", "ACTIV", "IN_ASTEPTARE", "FINALIZAT")

    val comenziFiltrate = when (filtruSelectat) {
        "TOATE" -> toateComenzile
        "ACTIV" -> toateComenzile.filter {
            it.status.equals("ACTIV", ignoreCase = true) || it.status.equals("ACTIVE", ignoreCase = true)
        }
        "IN_ASTEPTARE" -> toateComenzile.filter {
            it.status.contains("ASTEPTARE", ignoreCase = true)
        }
        "FINALIZAT" -> toateComenzile.filter {
            it.status.equals("FINALIZAT", ignoreCase = true)
        }
        else -> toateComenzile
    }

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.loadToateComenzileCloud(token)
            delay(5000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comenzi din Cloud", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Înapoi")
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.loadToateComenzileCloud(token) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reîncarcă")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filtre
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filtre.forEach { filtru ->
                    val isSelected = filtruSelectat == filtru
                    val (bgColor, textColor) = when {
                        !isSelected -> Color(0xFFF1F5F9) to Color.Gray
                        filtru == "ACTIV" -> Color(0xFF1976D2) to Color.White
                        filtru == "IN_ASTEPTARE" -> Color(0xFFE65100) to Color.White
                        filtru == "FINALIZAT" -> Color(0xFF2E7D32) to Color.White
                        else -> Color(0xFF455A64) to Color.White
                    }
                    Surface(
                        onClick = { filtruSelectat = filtru },
                        color = bgColor,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (filtru) {
                                "TOATE" -> "Toate"
                                "ACTIV" -> "Active"
                                "IN_ASTEPTARE" -> "Așteptare"
                                "FINALIZAT" -> "Finalizat"
                                else -> filtru
                            },
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            if (!isLoading && comenziFiltrate.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nu există comenzi pentru filtrul selectat.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        Text(
                            text = "${comenziFiltrate.size} comenzi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }

                    items(comenziFiltrate, key = { it.id }) { comanda ->
                        val isActiv = comanda.status.equals("ACTIV", ignoreCase = true) || comanda.status.equals("ACTIVE", ignoreCase = true)
                        val isAsteptare = comanda.status.contains("ASTEPTARE", ignoreCase = true)
                        val isFinalizat = comanda.status.equals("FINALIZAT", ignoreCase = true)

                        val (badgeColor, badgeText) = when {
                            isActiv -> Color(0xFF1976D2) to "ACTIV"
                            isAsteptare -> Color(0xFFE65100) to "ÎN AȘTEPTARE"
                            isFinalizat -> Color(0xFF2E7D32) to "FINALIZAT"
                            else -> Color.Gray to comanda.status
                        }

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
                                        tint = badgeColor
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = comanda.numePacient,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        color = badgeColor.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = badgeColor,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    thickness = 0.5.dp,
                                    color = Color.LightGray
                                )

                                Text(
                                    text = "📍 Salon ${comanda.nrSalon} · ${comanda.pat}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "💊 ${comanda.medicament}",
                                    fontSize = 15.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}