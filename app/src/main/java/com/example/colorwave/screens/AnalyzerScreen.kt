package com.example.colorwave.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.pow

@Composable
fun AnalyzerScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    var bassIntensity by remember { mutableStateOf(0f) }
    var midIntensity by remember { mutableStateOf(0f) }
    var highIntensity by remember { mutableStateOf(0f) }
    var globalVolume by remember { mutableStateOf(0f) }
    
    val sessionColors = remember { mutableStateListOf<Int>() }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasPermission = it }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // НЕЛИНЕЙНАЯ МОДЕЛЬ ЦВЕТА: Доминирующая частота выигрывает за счет 3-й степени
    val dynamicHue by remember(bassIntensity, midIntensity, highIntensity) {
        derivedStateOf {
            val b = bassIntensity.pow(3f)
            val m = midIntensity.pow(3f)
            val h = highIntensity.pow(3f)
            val total = b + m + h
            
            if (total < 0.0001f) 200f 
            else {
                // Бас (0° - Красный), Мид (120° - Зеленый), Высокие (240° - Синий)
                ((b * 0f + m * 120f + h * 240f) / total) % 360f
            }
        }
    }

    val colorTop by animateColorAsState(
        targetValue = if (globalVolume < 0.02f) Color.Black else Color.hsv(
            hue = dynamicHue,
            saturation = 0.9f,
            value = (0.2f + globalVolume * 2.5f).coerceIn(0.1f, 1f)
        ),
        animationSpec = tween(120, easing = LinearEasing)
    )

    val colorBottom by animateColorAsState(
        targetValue = if (globalVolume < 0.02f) Color(0xFF101015) else Color.hsv(
            hue = (dynamicHue + 60f) % 360f,
            saturation = 1f,
            value = (0.1f + globalVolume * 1.5f).coerceIn(0.05f, 0.7f)
        ),
        animationSpec = tween(250, easing = LinearEasing)
    )

    if (hasPermission) {
        LaunchedEffect(Unit) {
            val sampleRate = 44100
            val fftSize = 1024
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT).coerceAtLeast(fftSize)
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)

            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                withContext(Dispatchers.IO) {
                    val buffer = ShortArray(fftSize)
                    audioRecord.startRecording()
                    val smoothing = 0.3f 

                    try {
                        while (isActive) {
                            if (audioRecord.read(buffer, 0, fftSize) > 0) {
                                var b = 0f; var m = 0f; var h = 0f
                                var totalAmplitude = 0f
                                for (i in 0 until fftSize - 1) {
                                    val s1 = buffer[i].toInt().toFloat() / 32768f
                                    val s2 = buffer[i+1].toInt().toFloat() / 32768f
                                    val amp = abs(s1)
                                    totalAmplitude += amp
                                    val delta = abs(s1 - s2)

                                    // Разделение с гипер-усилением для синего
                                    when {
                                        delta < 0.015f -> b += amp * 8f   // Бас
                                        delta < 0.10f -> m += amp * 25f  // Средние (Зеленый)
                                        else -> h += amp * 180f         // Высокие (Синий)
                                    }
                                }
                                val n = fftSize.toFloat()
                                globalVolume = globalVolume * 0.7f + (totalAmplitude / n * 15f).coerceIn(0f, 1f) * 0.3f
                                bassIntensity = bassIntensity * (1 - smoothing) + (b / n).coerceIn(0f, 1f) * smoothing
                                midIntensity = midIntensity * (1 - smoothing) + (m / n).coerceIn(0f, 1f) * smoothing
                                highIntensity = highIntensity * (1 - smoothing) + (h / n).coerceIn(0f, 1f) * smoothing
                                
                                if (System.currentTimeMillis() % 400 < 20 && globalVolume > 0.05f) {
                                    sessionColors.add(colorTop.toArgb())
                                }
                            }
                            delay(16)
                        }
                    } finally { audioRecord.stop(); audioRecord.release() }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(colorTop, colorBottom, Color.Black)))) {
        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 48.dp, start = 16.dp)) {
            Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White)
        }
        Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
            Row(Modifier.height(200.dp).fillMaxWidth(), Arrangement.Center, Alignment.Bottom) {
                SpectrumBar(bassIntensity, Color(0xFFFF4444), "BASS")
                Spacer(Modifier.width(18.dp)); SpectrumBar(midIntensity, Color(0xFF44FF44), "MID")
                Spacer(Modifier.width(18.dp)); SpectrumBar(highIntensity, Color(0xFF4444FF), "HIGH")
            }
            Spacer(Modifier.height(100.dp))
            Button(
                onClick = {
                    val result = sessionColors.distinct().shuffled().take(5).ifEmpty { listOf(colorTop.toArgb(), colorBottom.toArgb()) }
                    val encoded = Uri.encode("live_${result.joinToString("_")}")
                    navController.navigate("music_result/$encoded")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.15f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(64.dp).padding(horizontal = 32.dp)
            ) {
                Icon(Icons.Default.StopCircle, null, tint = Color.White)
                Spacer(Modifier.width(12.dp)); Text("Завершить", color = Color.White)
            }
        }
    }
}

@Composable
fun SpectrumBar(intensity: Float, color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.width(55.dp).height((intensity * 200).dp.coerceAtLeast(8.dp)).background(color.copy(alpha = 0.8f), MaterialTheme.shapes.medium))
        Text(label, color = Color.White.copy(0.5f), style = MaterialTheme.typography.labelSmall)
    }
}
