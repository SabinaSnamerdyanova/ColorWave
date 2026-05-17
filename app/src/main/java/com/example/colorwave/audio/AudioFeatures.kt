package com.example.colorwave.audio

data class AudioFeatures(
    val energy: Float,
    val brightness: Float,
    val complexity: Float,
    val valence: Float,
    val seed: Long
)
