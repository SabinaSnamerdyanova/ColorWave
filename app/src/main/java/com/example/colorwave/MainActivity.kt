package com.example.colorwave

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.colorwave.screens.LoginScreen
import com.example.colorwave.screens.MainScreen
import com.example.colorwave.ui.theme.ColorWaveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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
    val viewModel: MainViewModel = viewModel()
    val prefs = remember { context.getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    val savedLogin = remember { prefs.getString("user_login", null) }
    if (savedLogin != null && viewModel.currentUser == null) {
        viewModel.login(savedLogin)
    }

    val navController = rememberNavController()
    val startDest = if (savedLogin != null) "main_app" else "login"

    NavHost(navController = navController, startDestination = startDest) {
        composable("login") {
            LoginScreen(navController = navController, viewModel = viewModel)
        }
        composable("main_app") {
            MainScreen(rootNavController = navController, mainViewModel = viewModel)
        }
    }
}
