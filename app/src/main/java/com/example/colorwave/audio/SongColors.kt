package com.example.colorwave.audio

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

object SongColors {

    fun fromFeatures(features: AudioFeatures): List<Color> {
        val energy = features.energy.coerceIn(0f, 1f)
        val brightness = features.brightness.coerceIn(0f, 1f)

        val baseHue = (energy * 280f + brightness * 80f) % 360f

        val saturation = 0.45f + energy * 0.5f
        val lightness = 0.35f + brightness * 0.35f

        return listOf(
            hsl(baseHue, saturation, lightness),
            hsl((baseHue + 35f) % 360f, saturation * 0.9f, (lightness + 0.08f).coerceAtMost(0.85f)),
            hsl((baseHue + 70f) % 360f, saturation * 0.8f, lightness)
        )
    }

    private fun hsl(h: Float, s: Float, l: Float): Color {
        val c = (1 - kotlin.math.abs(2 * l - 1)) * s
        val x = c * (1 - kotlin.math.abs((h / 60f) % 2 - 1))
        val m = l - c / 2

        val (r1, g1, b1) = when {
            h < 60 -> Triple(c, x, 0f)
            h < 120 -> Triple(x, c, 0f)
            h < 180 -> Triple(0f, c, x)
            h < 240 -> Triple(0f, x, c)
            h < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color(
            ((r1 + m) * 255).roundToInt(),
            ((g1 + m) * 255).roundToInt(),
            ((b1 + m) * 255).roundToInt()
        )
    }
}