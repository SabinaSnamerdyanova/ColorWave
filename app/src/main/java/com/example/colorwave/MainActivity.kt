package com.example.colorwave

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.colorwave.screens.LoginScreen
import com.example.colorwave.screens.MainScreen
import com.example.colorwave.ui.theme.ColorWaveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ColorWaveTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("prefs", Context.MODE_PRIVATE) }
    val isLoggedIn = remember { prefs.getBoolean("is_logged_in", false) }
    
    val navController = rememberNavController()

    NavHost(
        navController = navController, 
        startDestination = if (isLoggedIn) "main_app" else "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("main_app") {
            MainScreen(rootNavController = navController)
        }
    }
}
