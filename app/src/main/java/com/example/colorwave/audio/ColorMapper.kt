package com.example.colorwave.audio

import androidx.compose.ui.graphics.Color
import java.util.Random

object ColorMapper {

    fun fromFeatures(f: AudioFeatures): List<Color> {

        val rand = Random(f.seed)

        val energy = f.energy
        val complexity = f.complexity
        val brightness = f.brightness

        val archetype = when {
            energy > 0.75f && complexity > 0.5f -> "PULSE"
            brightness > 0.65f && energy > 0.5f -> "POP"
            complexity > 0.6f && brightness < 0.5f -> "NOIR"
            energy < 0.35f && brightness < 0.45f -> "EARTH"
            energy < 0.4f && brightness > 0.6f -> "SKY"
            complexity > 0.55f -> "NEON"
            else -> "METAL"
        }

        val hues = when (archetype) {

            "PULSE" -> listOf(0f, 20f, 45f, 320f)
            "POP" -> listOf(50f, 110f, 300f, 20f)
            "NEON" -> listOf(280f, 200f, 330f, 150f)
            "NOIR" -> listOf(210f, 260f, 290f)
            "EARTH" -> listOf(90f, 120f, 40f, 30f)
            "SKY" -> listOf(190f, 210f, 220f)
            else -> listOf(0f, 350f, 10f, 220f)
        }

        return List(5) { i ->
            val base = hues[i % hues.size]
            val jitter = (rand.nextFloat() - 0.5f) * 35f

            Color.hsv(
                hue = (base + jitter + 360f) % 360f,
                saturation = (0.4f + rand.nextFloat() * 0.5f)
                    .coerceIn(0.3f, 1f),
                value = (0.35f + rand.nextFloat() * 0.6f)
                    .coerceIn(0.3f, 1f)
            )
        }
    }
}