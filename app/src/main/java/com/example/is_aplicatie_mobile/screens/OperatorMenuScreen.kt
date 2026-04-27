package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.is_aplicatie_mobile.viewmodel.ModControl
import com.example.is_aplicatie_mobile.viewmodel.OperatorViewModel

@Composable
fun OperatorMenuScreen(
    viewModel: OperatorViewModel,
    onNavigateToSchimbareMod: () -> Unit,
    onNavigateToTeleghidare: () -> Unit
) {
    val modCurent by viewModel.modCurent.collectAsState()
    var afiseazaAlerta by remember { mutableStateOf(false) }

    if (afiseazaAlerta) {
        AlertDialog(
            onDismissRequest = { afiseazaAlerta = false },
            title = { Text("Acces Respins") },
            text = { Text("Disponibil doar în mod teleghidare.") }, //
            confirmButton = {
                TextButton(onClick = { afiseazaAlerta = false }) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { /* TODO: Logica de conectare robot */ }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Conectare robot") // [cite: 87]
        }

        Button(onClick = onNavigateToSchimbareMod, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Schimbare Mod") // [cite: 89]
        }

        Button(
            onClick = {
                if (modCurent == ModControl.TELEGHIDARE) {
                    onNavigateToTeleghidare()
                } else {
                    afiseazaAlerta = true // Declansăm alerta dacă nu e setat corect
                }
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Teleghidare") // [cite: 89]
        }
    }
}