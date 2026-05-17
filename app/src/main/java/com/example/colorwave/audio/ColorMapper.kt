package com.example.colorwave.audio

import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

object ColorMapper {

    fun fromFeatures(f: AudioFeatures): List<Color> {
        val rand = Random(f.seed)

        val character = ((f.energy * 10 +
                f.complexity * 7 +
                f.valence * 5).toInt() + rand.nextInt()) % 6

        return when (character) {
            0 -> impulse(rand)
            1 -> flow(rand)
            2 -> organic(rand)
            3 -> atmosphere(rand)
            4 -> mechanical(rand)
            else -> eclectic(rand)
        }
    }

    private fun impulse(r: Random) = palette(
        r, 0f..60f, 0.75f, 1f, 0.6f, 1f
    )

    private fun flow(r: Random) = palette(
        r, 180f..240f, 0.4f, 0.9f, 0.5f, 1f
    )

    private fun organic(r: Random) = palette(
        r, 70f..150f, 0.35f, 0.8f, 0.45f, 0.85f
    )

    private fun atmosphere(r: Random) = palette(
        r, 250f..330f, 0.3f, 0.8f, 0.4f, 0.9f
    )

    private fun mechanical(r: Random): List<Color> {
        val base = palette(r, 0f..360f, 0.05f, 0.15f, 0.3f, 0.6f)
        val accent = Color.hsv(r.nextFloat() * 360f, 0.9f, 0.9f)
        return base.dropLast(1) + accent
    }

    private fun eclectic(r: Random): List<Color> =
        List(5) {
            Color.hsv(
                r.nextFloat() * 360f,
                r.nextFloat() * 0.6f + 0.4f,
                r.nextFloat() * 0.5f + 0.4f
            )
        }

    private fun palette(
        r: Random,
        hueRange: ClosedFloatingPointRange<Float>,
        sMin: Float,
        sMax: Float,
        vMin: Float,
        vMax: Float
    ): List<Color> {
        val baseHue = r.nextFloat() *
                (hueRange.endInclusive - hueRange.start) + hueRange.start

        return List(5) { i ->
            val hue = (baseHue + sin(i * 0.8f) * 40f + r.nextFloat() * 20f) % 360
            val sat = sMin + r.nextFloat() * (sMax - sMin)
            val value = vMin + r.nextFloat() * (vMax - vMin)
            Color.hsv(hue, sat, value)
        }
    }
}