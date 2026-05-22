package com.example.colorwave.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.example.colorwave.MainViewModel
import com.example.colorwave.data.PaletteEntity
import com.example.colorwave.utils.paletteBitmap
import com.example.colorwave.utils.sharePalette
import com.example.colorwave.utils.saveToGallery

@Composable
fun CollectionScreen(viewModel: MainViewModel) {

    val palettes by viewModel.userPalettes.collectAsState()
    var selectedPalette by remember { mutableStateOf<PaletteEntity?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Моя коллекция",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (palettes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("У вас пока нет сохраненных палитр", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(palettes) { palette ->
                    PaletteItem(
                        palette = palette,
                        onClick = { selectedPalette = palette },
                        onDelete = { viewModel.deletePalette(palette) }
                    )
                }
            }
        }
    }

    selectedPalette?.let { palette ->

        val colors = palette.colorsHex.split(",").mapNotNull {
            try {
                Color(AndroidColor.parseColor(it))
            } catch (_: Exception) {
                null
            }
        }

        val hexList = colors.map {
            String.format("#%06X", 0xFFFFFF and it.toArgb())
        }

        Dialog(onDismissRequest = { selectedPalette = null }) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = colors,
                                start = Offset.Zero,
                                end = Offset(1200f, 2000f)
                            )
                        )
                        .clickable { selectedPalette = null }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    IconButton(onClick = {

                        val bitmap = paletteBitmap(colors.map { it.toArgb() })

                        sharePalette(
                            context = context,
                            bitmap = bitmap,
                            title = palette.trackName,
                            hexList = hexList
                        )

                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }

                    IconButton(onClick = {
                        saveToGallery(context, colors)
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(
                        "HEX COLORS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    hexList.forEachIndexed { index, hex ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors[index])
                            )

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text = hex,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PaletteItem(
    palette: PaletteEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {

    val colors = palette.colorsHex.split(",").mapNotNull {
        try {
            Color(AndroidColor.parseColor(it))
        } catch (_: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Row(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .padding(12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.chunked(2).take(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = palette.trackName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = java.text.SimpleDateFormat("dd.MM.yyyy")
                            .format(palette.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}