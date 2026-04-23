package com.example.colorwave.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

enum class AppTheme { LIGHT, DARK, SYSTEM }

object ThemeConfig {
    var currentTheme by mutableStateOf(AppTheme.SYSTEM)
}