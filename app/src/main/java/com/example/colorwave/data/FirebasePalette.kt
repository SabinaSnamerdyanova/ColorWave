package com.example.colorwave.data

data class FirebasePalette(
    val id: String = "",
    val trackName: String = "",
    val colorsHex: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)