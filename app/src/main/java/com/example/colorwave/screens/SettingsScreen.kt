package com.example.colorwave.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.colorwave.ui.theme.AppTheme
import com.example.colorwave.ui.theme.ThemeConfig

@Composable
fun SettingsScreenContent() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Настройки темы", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Радиокнопки для выбора темы
        ThemeOption("Светлая", AppTheme.LIGHT)
        ThemeOption("Темная", AppTheme.DARK)
        ThemeOption("Системная", AppTheme.SYSTEM)
    }
}

@Composable
fun ThemeOption(text: String, theme: AppTheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        RadioButton(
            selected = ThemeConfig.currentTheme == theme,
            onClick = { ThemeConfig.currentTheme = theme }
        )
        Text(text)
    }
}