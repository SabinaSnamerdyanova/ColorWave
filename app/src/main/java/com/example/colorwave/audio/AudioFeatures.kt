package com.example.colorwave.audio

data class AudioFeatures(
    val energy: Float,
    val brightness: Float,
    val valence: Float,
    val complexity: Float,
    val seed: Long
)