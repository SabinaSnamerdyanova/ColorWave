package com.example.colorwave.screens

import android.content.*
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.colorwave.audio.ColorMapper
import com.example.colorwave.audio.FileAnalyzer
import com.example.colorwave.spotify.SpotifyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicResultScreen(navController: NavHostController, fileUri: String) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var palette by remember { mutableStateOf<List<Color>>(listOf(Color.DarkGray, Color.Gray)) }
    var isFullScreen by remember { mutableStateOf(false) }

    val decoded = Uri.decode(fileUri)

    LaunchedEffect(decoded) {
        isLoading = true
        withContext(Dispatchers.Default) {
            palette = try {
                if (decoded.startsWith("live_")) {
                    val parts = decoded.removePrefix("live_").split("_")
                    parts.mapNotNull { hex ->
                        try { Color(hex.toInt()) } catch (_: Exception) { null }
                    }.ifEmpty { listOf(Color.Gray, Color.DarkGray) }
                } else if (decoded.startsWith("spotify:") || decoded.length == 22) {
                    val trackId = decoded.removePrefix("spotify:track:")
                    val features = SpotifyService.loadFeatures(
                        trackId = trackId,
                        seed = decoded.hashCode().toLong()
                    )
                    ColorMapper.fromFeatures(features)
                } else {
                    val features = FileAnalyzer.analyze(context, Uri.parse(decoded))
                    ColorMapper.fromFeatures(features)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                listOf(Color.DarkGray, Color.Gray)
            }
            isLoading = false
        }
    }

    if (isFullScreen) {
        FullScreenGradient(palette = palette, onDismiss = { isFullScreen = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Палитра звука") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (!isLoading) {
                        IconButton(onClick = { sharePalette(context, palette) }) {
                            Icon(Icons.Default.Share, contentDescription = "Поделиться")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(strokeWidth = 4.dp)
                    Spacer(Modifier.height(16.dp))
                    Text("Вслушиваемся в характер звука…", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // КАРТОЧКА С ГРАДИЕНТОМ (Теперь кликабельная и с исправленными отступами)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .clickable { isFullScreen = true },
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = palette,
                                    start = Offset.Zero,
                                    end = Offset.Infinite // Растягиваем на весь размер карточки
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Нажмите на карточку, чтобы развернуть", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(Modifier.height(20.dp))

                palette.forEach { color ->
                    val hex = String.format("#%06X", 0xFFFFFF and color.toArgb())
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(color))
                            Spacer(Modifier.width(16.dp))
                            Text(hex, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            IconButton(onClick = {
                                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("HEX", hex))
                                Toast.makeText(context, "HEX скопирован", Toast.LENGTH_SHORT).show()
                            }) { Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp)) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { saveToGallery(context, palette) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Download, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Скачать PNG")
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun FullScreenGradient(palette: List<Color>, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors = palette, start = Offset.Zero, end = Offset.Infinite))
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(50))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White)
            }
        }
    }
}

private fun sharePalette(context: Context, palette: List<Color>) {
    val hexString = palette.joinToString("\n") { String.format("#%06X", 0xFFFFFF and it.toArgb()) }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Моя палитра ColorWave:\n$hexString")
    }
    context.startActivity(Intent.createChooser(intent, "Поделиться палитрой"))
}

private fun saveToGallery(context: Context, palette: List<Color>) {
    val width = 1440
    val height = 2560
    val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(b)
    val colors = palette.map { it.toArgb() }.toIntArray()

    val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), colors, null, Shader.TileMode.CLAMP)
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    val filename = "ColorWave_${System.currentTimeMillis()}.png"
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
    }

    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    try {
        uri?.let { targetUri ->
            context.contentResolver.openOutputStream(targetUri)?.use { out ->
                b.compress(Bitmap.CompressFormat.PNG, 100, out)
                Toast.makeText(context, "Изображение сохранено!", Toast.LENGTH_SHORT).show()
            }
        } ?: throw Exception("MediaStore Error")
    } catch (_: Exception) {
        Toast.makeText(context, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
    }
}
