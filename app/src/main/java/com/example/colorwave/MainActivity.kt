package com.example.colorwave
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.colorwave.screens.MainScreen
import com.example.colorwave.ui.theme.AppTheme
import com.example.colorwave.ui.theme.ColorWaveTheme
import com.example.colorwave.ui.theme.ThemeConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ColorWaveTheme() {
                MainScreen()
            }
        }
    }
}

