package com.example.colorwave.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.colorwave.MainViewModel
import com.example.colorwave.audio.ColorMapper
import com.example.colorwave.audio.FileAnalyzer
import com.example.colorwave.utils.paletteBitmap
import com.example.colorwave.utils.saveToGallery
import com.example.colorwave.utils.sharePalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicResultScreen(
    navController: NavHostController,
    fileUri: String,
    mainViewModel: MainViewModel
) {

    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var palette by remember { mutableStateOf<List<Color>>(emptyList()) }
    var trackTitle by remember { mutableStateOf("") }
    var showFullScreen by remember { mutableStateOf(false) }

    val decoded = android.net.Uri.decode(fileUri)

    LaunchedEffect(decoded) {
        isLoading = true

        withContext(Dispatchers.Default) {
            try {
                val features = FileAnalyzer.analyze(context, android.net.Uri.parse(decoded))
                palette = ColorMapper.fromFeatures(features)
            } catch (_: Exception) {
                palette = listOf(Color.DarkGray, Color.Gray)
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {

                    IconButton(onClick = {

                        val bitmap = paletteBitmap(palette.map { it.toArgb() })

                        val hexList = palette.map {
                            String.format("#%06X", 0xFFFFFF and it.toArgb())
                        }

                        sharePalette(
                            context = context,
                            bitmap = bitmap,
                            title = trackTitle,
                            hexList = hexList
                        )

                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(onClick = {

                        try {
                            val bitmap = paletteBitmap(palette.map { it.toArgb() })
                            saveToGallery(context, palette)

                            Toast.makeText(context, "Сохранено в галерею", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
                        }

                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Download")
                    }
                }
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { showFullScreen = true }
                        .background(
                            Brush.linearGradient(
                                colors = palette,
                                start = Offset.Zero,
                                end = Offset(1200f, 2000f)
                            )
                        )
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Нажмите, чтобы открыть палитру",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(20.dp))

                palette.forEach { color ->

                    val hex = String.format("#%06X", 0xFFFFFF and color.toArgb())

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(color)
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = hex,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        mainViewModel.savePalette(trackTitle, palette)
                        Toast.makeText(context, "Сохранено в коллекцию!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Сохранить в коллекцию",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showFullScreen) {

        Dialog(onDismissRequest = { showFullScreen = false }) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullScreen = false }
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = palette,
                            start = Offset.Zero,
                            end = Offset(1200f, 2000f)
                        )
                    )
            )
        }
    }
}