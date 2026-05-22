package com.example.colorwave.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun sharePalette(
    context: Context,
    bitmap: Bitmap,
    title: String,
    hexList: List<String>
) {

    try {
        val file = File(context.cacheDir, "palette.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val hexText = buildString {
            append("🎨 HEX COLORS:\n")
            hexList.forEach { append("$it\n") }
        }

        val text = buildString {
            append("🎵 $title\n\n")
            append(hexText)
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, "Поделиться палитрой")
        )

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Ошибка шаринга", Toast.LENGTH_SHORT).show()
    }
}