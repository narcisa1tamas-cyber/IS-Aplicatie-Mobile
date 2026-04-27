package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.is_aplicatie_mobile.viewmodel.ModControl
import com.example.is_aplicatie_mobile.viewmodel.OperatorViewModel
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(viewModel: OperatorViewModel, onBack: () -> Unit) {
    val modCurent by viewModel.modCurent.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schimbare Mod", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Înapoi")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA) // Fundal gri deschis pentru contrast
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Selectați modul de operare pentru robot",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            // Mod Automat - Control din Cloud
            SelectionCard(
                title = "Mod Automat",
                subtitle = "Execută comenzile primite din Cloud",
                icon = Icons.Default.SettingsSuggest,
                isSelected = modCurent == ModControl.AUTOMAT,
                onClick = { viewModel.schimbaModControl(ModControl.AUTOMAT) }
            )

            // Mod Teleghidat - Control Manual
            SelectionCard(
                title = "Mod Teleghidat",
                subtitle = "Control manual prin interfața aplicației",
                icon = Icons.Default.SmartToy,
                isSelected = modCurent == ModControl.TELEGHIDARE,
                onClick = { viewModel.schimbaModControl(ModControl.TELEGHIDARE) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Mesaj informativ jos
            Text(
                text = "Schimbarea modului va afecta comportamentul robotului în timp real.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun SelectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 2.dp),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF1976D2)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Iconița în cerc (opțional, pentru un look mai medical/profi)
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF1F5F9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF1976D2) else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isSelected) Color(0xFF1976D2) else Color.Black
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
            }

            if (isSelected) {
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "ACTIV",
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