package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.is_aplicatie_mobile.viewmodel.OperatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeleoperationScreen(viewModel: OperatorViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control Robot", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Înapoi")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Această linie pune joystick-ul pe centrul vertical
        ) {

            // Container pentru Joystick pentru a-i oferi spațiu generos
            Box(
                modifier = Modifier.weight(1f), // Ocupă spațiul disponibil
                contentAlignment = Alignment.Center
            ) {
                JoystickController(
                    onDirectionClick = { direction ->
                        viewModel.trimiteComandaDirectie(direction)
                    }
                )
            }

            // Textul rămâne jos
            Text(
                text = "Folosiți săgețile pentru a controla robotul.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun JoystickController(onDirectionClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(300.dp) // Am mărit puțin dimensiunea pentru a semăna mai mult cu imaginea
            .shadow(15.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Cercul exterior (gri foarte deschis)
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9))
        )

        // Săgețile direcționale
        JoystickArrow(Icons.Default.ArrowDropUp, "FATA", Modifier.align(Alignment.TopCenter).padding(top = 10.dp), onDirectionClick)
        JoystickArrow(Icons.Default.ArrowDropDown, "SPATE", Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp), onDirectionClick)
        JoystickArrow(Icons.Default.ArrowLeft, "STANGA", Modifier.align(Alignment.CenterStart).padding(start = 10.dp), onDirectionClick)
        JoystickArrow(Icons.Default.ArrowRight, "DREAPTA", Modifier.align(Alignment.CenterEnd).padding(end = 10.dp), onDirectionClick)

        // Butonul central (design minimalist conform imaginii)
        Surface(
            modifier = Modifier
                .size(100.dp)
                .shadow(8.dp, CircleShape),
            shape = CircleShape,
            color = Color.White
        ) {
            // Putem lăsa gol sau adăuga un mic indicator de stop
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1976D2).copy(alpha = 0.2f))
                )
            }
        }
    }
}

@Composable
fun JoystickArrow(
    icon: ImageVector,
    direction: String,
    modifier: Modifier,
    onDirectionClick: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val tintColor = if (isPressed) Color(0xFF1976D2) else Color(0xFF94A3B8)

    Box(
        modifier = modifier
            .size(80.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onDirectionClick(direction) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = direction,
            tint = tintColor,
            modifier = Modifier.size(45.dp)
        )
    }
}