package com.example.colorwave.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

fun paletteBitmap(colors: List<Int>): Bitmap {
    val width = 1080
    val height = 1080

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val shader = android.graphics.LinearGradient(
        0f,
        0f,
        width.toFloat(),
        0f,
        colors.toIntArray(),
        null,
        android.graphics.Shader.TileMode.CLAMP
    )

    paint.shader = shader
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    return bitmap
}