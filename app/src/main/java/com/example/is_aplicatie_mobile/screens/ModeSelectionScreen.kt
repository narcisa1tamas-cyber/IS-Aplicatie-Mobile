package com.example.is_aplicatie_mobile.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.is_aplicatie_mobile.viewmodel.ModControl
import com.example.is_aplicatie_mobile.viewmodel.OperatorViewModel

private val PrimaryBlue   = Color(0xFF1565C0)
private val LightBlue     = Color(0xFF1976D2)
private val SoftBlue      = Color(0xFFE3F2FD)
private val DeepBlue      = Color(0xFF0D47A1)
private val AccentTeal    = Color(0xFF00897B)
private val SoftTeal      = Color(0xFFE0F2F1)
private val PageBg        = Color(0xFFF0F4F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(
    viewModel: OperatorViewModel,
    onBack: () -> Unit,
    token: String = "",
    numeOperator: String = ""
) {
    val modCurent     by viewModel.modCurent.collectAsState()
    val operatorActiv by viewModel.operatorTeleghidare.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mod de Operare",
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = PageBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Alege modul de control al robotul",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            ModeCard(
                title = "Mod Automat",
                subtitle = "Robotul execută comenzile primite din Cloud fără intervenție manuală.",
                features = listOf("Livrări autonome", "Urmărire traseu", "Notificări automate"),
                icon = Icons.Default.SettingsSuggest,
                isSelected = modCurent == ModControl.AUTOMAT,
                selectedColor = LightBlue,
                selectedBgColor = SoftBlue,
                onClick = {
                    viewModel.schimbaModControlCuOperator(ModControl.AUTOMAT, token, numeOperator)
                }
            )

            ModeCard(
                title = "Mod Teleghidat",
                subtitle = "Controlezi robotul manual din aplicație în timp real.",
                features = listOf("Control direcție live", "Flux video", "Răspuns imediat"),
                icon = Icons.Default.SmartToy,
                isSelected = modCurent == ModControl.TELEGHIDARE,
                selectedColor = AccentTeal,
                selectedBgColor = SoftTeal,
                onClick = {
                    viewModel.schimbaModControlCuOperator(ModControl.TELEGHIDARE, token, numeOperator)
                }
            )
            if (modCurent == ModControl.TELEGHIDARE && !operatorActiv.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.4f)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SoftTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = AccentTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Operator activ",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = operatorActiv!!,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentTeal,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Schimbarea modului afectează comportamentul robotului în timp real.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    subtitle: String,
    features: List<String>,
    icon: ImageVector,
    isSelected: Boolean,
    selectedColor: Color,
    selectedBgColor: Color,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) selectedColor else Color(0xFFE0E0E0)
    val bgColor     = if (isSelected) selectedBgColor.copy(alpha = 0.35f) else Color.White
    val iconBg      = if (isSelected) selectedColor.copy(alpha = 0.15f) else Color(0xFFF1F5F9)
    val iconTint    = if (isSelected) selectedColor else Color.Gray
    val titleColor  = if (isSelected) selectedColor else Color(0xFF212121)

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 1.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Iconița mare
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = titleColor,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Surface(
                            color = selectedColor,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "ACTIV",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Lista de feature-uri
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) selectedColor else Color.LightGray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            color = if (isSelected) selectedColor.copy(alpha = 0.8f) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
