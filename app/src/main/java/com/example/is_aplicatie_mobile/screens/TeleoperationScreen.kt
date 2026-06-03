package com.example.is_aplicatie_mobile.screens

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.is_aplicatie_mobile.viewmodel.OperatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeleoperationScreen(viewModel: OperatorViewModel, onBack: () -> Unit) {

    val videoUrl by viewModel.videoStreamUrl.collectAsState()

    val handleBack = {
        viewModel.stopVideoStream()
        onBack()
    }

    BackHandler(onBack = handleBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control Robot", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (videoUrl != null) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = false
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            loadUrl(videoUrl!!)
                        }
                    },
                    update = { it.loadUrl(videoUrl!!) }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF1A1A2E), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Camera inactivă",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.startVideoStream() }) {
                            Text("Pornește camera")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                JoystickController(
                    onDirectionPress = { direction -> viewModel.trimiteComandaDirectie(direction) },
                    onRelease = { viewModel.trimiteStop() }
                )
            }

            Text(
                text = "Țineți apăsat pe săgeți pentru a mișca robotul. Eliberați pentru oprire.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun JoystickController(
    onDirectionPress: (String) -> Unit,
    onRelease: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(300.dp)
            .shadow(15.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9))
        )

        JoystickArrow(Icons.Default.ArrowDropUp, "FATA", Modifier.align(Alignment.TopCenter).padding(top = 10.dp), onDirectionPress, onRelease)
        JoystickArrow(Icons.Default.ArrowDropDown, "SPATE", Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp), onDirectionPress, onRelease)
        JoystickArrow(Icons.Default.ArrowLeft, "STANGA", Modifier.align(Alignment.CenterStart).padding(start = 10.dp), onDirectionPress, onRelease)
        JoystickArrow(Icons.Default.ArrowRight, "DREAPTA", Modifier.align(Alignment.CenterEnd).padding(end = 10.dp), onDirectionPress, onRelease)

        Surface(
            modifier = Modifier
                .size(100.dp)
                .shadow(8.dp, CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onRelease() })
                },
            shape = CircleShape,
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD32F2F).copy(alpha = 0.7f))
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
    onPress: (String) -> Unit,
    onRelease: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (!isPressed) {
            onRelease()
        }
    }

    val tintColor = if (isPressed) Color(0xFF1976D2) else Color(0xFF94A3B8)

    Box(
        modifier = modifier
            .size(80.dp)
            .pointerInput(interactionSource) {
                detectTapGestures(
                    onPress = { offset ->
                        val pressInteraction = androidx.compose.foundation.interaction.PressInteraction.Press(offset)
                        interactionSource.emit(pressInteraction)

                        onPress(direction)

                        tryAwaitRelease()

                        interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(pressInteraction))
                    }
                )
            },
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
