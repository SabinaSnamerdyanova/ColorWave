package com.example.colorwave.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.colorwave.ui.theme.AppTheme
import com.example.colorwave.ui.theme.ThemeConfig

@Composable
fun SettingsScreenContent(rootNavController: NavHostController) {
    val context = LocalContext.current
    
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Настройки темы", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        ThemeOption("Светлая", AppTheme.LIGHT)
        ThemeOption("Темная", AppTheme.DARK)
        ThemeOption("Системная", AppTheme.SYSTEM)

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = {
                val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("is_logged_in", false).apply()

                rootNavController.navigate("login") {
                    popUpTo("main_app") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Выйти из аккаунта",
                color = Color.Red,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ThemeOption(text: String, theme: AppTheme) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = ThemeConfig.currentTheme == theme,
            onClick = { ThemeConfig.currentTheme = theme }
        )
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
