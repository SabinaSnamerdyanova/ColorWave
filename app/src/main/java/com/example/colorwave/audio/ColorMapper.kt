package com.example.colorwave.audio

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.Random

object ColorMapper {

    fun fromFeatures(f: AudioFeatures): List<Color> {
        val rand = Random(f.seed)
        val wEnergy = f.energy.coerceIn(0f, 1f)
        val wBright = f.brightness.coerceIn(0f, 1f)
        val wComplex = f.complexity.coerceIn(0f, 1f)

        val totalWeight = wEnergy + wBright + wComplex + 0.001f
        val hueBase = (wEnergy * 0f + wComplex * 140f + wBright * 240f) / totalWeight
        val moodShift = when {
            f.valence < 0.4f -> 210f
            f.valence > 0.7f -> 30f
            else -> hueBase
        }

        return List(5) { i ->
            val jitter = (rand.nextFloat() - 0.5f) * 30f
            val finalHue = (moodShift + jitter + (i * 15f)) % 360f
            
            Color.hsv(
                hue = if (finalHue < 0) finalHue + 360f else finalHue,
                saturation = (0.4f + f.energy * 0.5f - i * 0.05f).coerceIn(0.2f, 1f),
                value = (0.3f + f.valence * 0.6f + f.energy * 0.1f).coerceIn(0.2f, 1f)
            )
        }.sortedBy {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(it.toArgb(), hsv)
            hsv[0] 
        }
    }
}
