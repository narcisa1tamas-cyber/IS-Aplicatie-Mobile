package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReportsScreen(onBack: () -> Unit) {
    // Stare pentru a controla afișarea ferestrei de alertă (pop-up)
    var showSuccessAlert by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(20.dp)
        ) {
            Text(
                text = "PAGINA RAPOARTE",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1976D2),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 20.dp)
            )

            Text(
                text = "Date despre transport:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // text asa doar de test
            Column(modifier = Modifier.weight(1f)) {
                ReportItem("Pacient 3 Ora 10", "Medicament paracetamol", "in asteptare")
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Butonul Trimite raport
                Button(
                    onClick = { showSuccessAlert = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4511E))
                ) {
                    Text("Trimite raport")
                }

                // Butonul Back
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Back")
                }
            }
        }
    }

    // Mesajul de alertă care apare după "Trimite raport"
    if (showSuccessAlert) {
        AlertDialog(
            onDismissRequest = { showSuccessAlert = false },
            confirmButton = {
                TextButton(
                    onClick = { showSuccessAlert = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                    modifier = Modifier.background(Color(0xFFF4511E))
                ) {
                    Text("Ok")
                }
            },
            title = { Text("Mesaj alerta", fontWeight = FontWeight.Bold) },
            text = { Text("Raport trimis cu succes!") }
        )
    }
}

@Composable
fun ReportItem(pacient: String, medicament: String, status: String) {
    Column {
        Text(text = pacient, fontWeight = FontWeight.Medium)
        Text(text = medicament)
        Text(text = "Status ... $status")
    }
}