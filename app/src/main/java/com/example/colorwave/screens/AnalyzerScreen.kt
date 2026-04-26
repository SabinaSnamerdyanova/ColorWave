package com.example.colorwave.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.*

@Composable
fun AnalyzerScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    var bassVal by remember { mutableStateOf(0f) }
    var midVal by remember { mutableStateOf(0f) }
    var highVal by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("Ожидание звука...") }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasPermission = isGranted }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val color1 by animateColorAsState(
        targetValue = Color(
            red = (bassVal * 1.2f).coerceIn(0f, 1f),
            green = (midVal * 0.5f).coerceIn(0f, 1f),
            blue = (highVal * 0.2f).coerceIn(0f, 1f)
        ).copy(alpha = 1f),
        animationSpec = tween(100)
    )

    val color2 by animateColorAsState(
        targetValue = Color(
            red = (highVal * 0.3f).coerceIn(0f, 1f),
            green = (midVal * 1.0f).coerceIn(0f, 1f),
            blue = (bassVal * 0.8f).coerceIn(0f, 1f)
        ).copy(alpha = 1f),
        animationSpec = tween(150)
    )

    if (hasPermission) {
        LaunchedEffect(Unit) {
            val sampleRate = 44100
            val fftSize = 1024
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                .coerceAtLeast(fftSize)

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                statusText = "Ошибка инициализации"
                return@LaunchedEffect
            }

            statusText = "Анализирую частоты..."
            
            withContext(Dispatchers.IO) {
                val buffer = ShortArray(fftSize)
                audioRecord.startRecording()

                try {
                    while (isActive) {
                        val readSize = audioRecord.read(buffer, 0, fftSize)
                        if (readSize > 0) {
                            var b = 0f; var m = 0f; var h = 0f
                            
                            for (i in 0 until readSize / 2) {
                                val diff = abs(buffer[i+1].toInt() - buffer[i].toInt()).toFloat() / 32768f
                                val amp = abs(buffer[i].toInt()).toFloat() / 32768f

                                if (diff < 0.05f) b += amp * 2.0f
                                else if (diff < 0.2f) m += amp * 1.5f
                                else h += amp * 3.0f
                            }

                            val norm = readSize / 2f
                            bassVal = (b / norm * 10f).coerceIn(0f, 1f)
                            midVal = (m / norm * 15f).coerceIn(0f, 1f)
                            highVal = (h / norm * 25f).coerceIn(0f, 1f)
                        }
                        delay(20)
                    }
                } finally {
                    audioRecord.stop()
                    audioRecord.release()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(color1, color2, Color.Black)))
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 48.dp, start = 16.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(statusText, color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelLarge)
            
            Spacer(modifier = Modifier.height(60.dp))

            Row(
                modifier = Modifier.height(200.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                SpectrumBar(bassVal, Color(0xFFFF4444), "BASS")
                Spacer(Modifier.width(12.dp))
                SpectrumBar(midVal, Color(0xFF44FF44), "MID")
                Spacer(Modifier.width(12.dp))
                SpectrumBar(highVal, Color(0xFF4444FF), "HIGH")
            }

            Spacer(modifier = Modifier.height(60.dp))

            val hex = String.format("#%06X", (0xFFFFFF and color1.value.toLong().toInt()))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(
                    text = hex,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

@Composable
fun SpectrumBar(valNormalized: Float, color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .width(45.dp)
                .height((valNormalized * 200).dp.coerceAtLeast(10.dp))
                .background(
                    Brush.verticalGradient(listOf(color, color.copy(alpha = 0.3f))),
                    MaterialTheme.shapes.small
                )
        )
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.bodySmall)
    }
}
