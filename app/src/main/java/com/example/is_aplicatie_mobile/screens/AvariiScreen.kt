package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.is_aplicatie_mobile.model.Alarma
import com.example.is_aplicatie_mobile.viewmodel.NurseViewModel
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ErrorRed = Color(0xFFC62828)
private val LightRed = Color(0xFFFFEBEE)
private val WarningOrange = Color(0xFFE65100)
private val LightOrange = Color(0xFFFFF3E0)
private val ResolvedGreen = Color(0xFF2E7D32)
private val LightGreen = Color(0xFFE8F5E9)
private val PageBackground = Color(0xFFF4F8FB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvariiScreen(
    viewModel: NurseViewModel,
    token: String,
    onBack: () -> Unit
) {
    val alarme by viewModel.alarme.collectAsState()
    val isLoading by viewModel.isLoadingAlarme.collectAsState()

    var dataFiltru by remember { mutableStateOf<LocalDate?>(null) }
    var arataSelectorData by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val alarmeFiltrate = remember(alarme, dataFiltru) {
        if (dataFiltru == null) alarme
        else alarme.filter { alarma ->
            alarma.oraAlarma.take(10) == dataFiltru.toString()
        }
    }

    val alarmeActive    = alarmeFiltrate.filter { !it.rezolvata }
    val alarmeRezolvate = alarmeFiltrate.filter { it.rezolvata }

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.loadAlarme(token)
            delay(10_000)
        }
    }

    if (arataSelectorData) {
        DatePickerDialog(
            onDismissRequest = { arataSelectorData = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dataFiltru = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    arataSelectorData = false
                }) {
                    Text("Selectează", fontWeight = FontWeight.Bold, color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { arataSelectorData = false }) {
                    Text("Anulează", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            Surface(color = Color.White, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi",
                            tint = ErrorRed
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(LightRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReportProblem,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Avarii Robot",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = ErrorRed
                        )
                        Text(
                            text = "${alarmeActive.size} active · ${alarmeRezolvate.size} rezolvate",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = { viewModel.loadAlarme(token) },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = ErrorRed
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reîmprospătează",
                                tint = ErrorRed
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Bara de filtre ──────────────────────────────────────
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = dataFiltru == null,
                        onClick = { dataFiltru = null },
                        label = { Text("Toate", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ErrorRed,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = dataFiltru == LocalDate.now(),
                        onClick = { dataFiltru = LocalDate.now() },
                        label = { Text("Azi", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ErrorRed,
                            selectedLabelColor = Color.White
                        )
                    )

                    if (dataFiltru != null && dataFiltru != LocalDate.now()) {
                        FilterChip(
                            selected = true,
                            onClick = { arataSelectorData = true },
                            label = {
                                Text(
                                    dataFiltru!!.format(DateTimeFormatter.ofPattern("dd MMM")),
                                    fontSize = 13.sp
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Șterge filtru",
                                    modifier = Modifier.size(14.dp)
                                        .clickable { dataFiltru = null }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ErrorRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { arataSelectorData = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Alege dată",
                            tint = ErrorRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

        if (!isLoading && alarmeFiltrate.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(LightGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ResolvedGreen,
                        modifier = Modifier.size(52.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Nicio avarie înregistrată",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ResolvedGreen
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Robotul funcționează normal.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (alarmeActive.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ErrorRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVE (${alarmeActive.size})",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = ErrorRed,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    items(alarmeActive, key = { "active_${it.idAlarma}" }) { alarma ->
                        AlarmaCard(
                            alarma = alarma,
                            onRezolva = { viewModel.rezolvaAlarma(token, alarma.idAlarma) }
                        )
                    }
                }

                if (alarmeRezolvate.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ResolvedGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REZOLVATE (${alarmeRezolvate.size})",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = ResolvedGreen,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    items(alarmeRezolvate, key = { "rezolvat_${it.idAlarma}" }) { alarma ->
                        AlarmaCard(
                            alarma = alarma,
                            onRezolva = null
                        )
                    }
                }
            }
        }
        } // închide Column exterior
    }
}

@Composable
private fun AlarmaCard(
    alarma: Alarma,
    onRezolva: (() -> Unit)?
) {
    val isRezolvat = alarma.rezolvata
    val accentColor = if (isRezolvat) ResolvedGreen else ErrorRed
    val bgColor = if (isRezolvat) LightGreen else LightRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = accentColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRezolvat) Icons.Default.CheckCircle
                        else Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alarma.tip,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = accentColor
                    )
                    Text(
                        text = "#${alarma.idAlarma}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    color = accentColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isRezolvat) "REZOLVAT" else "ACTIVĂ",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            if (alarma.mesaj.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = accentColor.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = alarma.mesaj,
                    fontSize = 14.sp,
                    color = Color(0xFF37474F),
                    lineHeight = 20.sp
                )
            }

            if (alarma.oraAlarma.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🕐 ${alarma.oraAlarma}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (!isRezolvat && onRezolva != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onRezolva,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ResolvedGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Marchează ca rezolvată",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
