package com.example.colorwave

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.colorwave.screens.HomeScreenContent
import com.example.colorwave.screens.SettingsScreenContent
import com.example.colorwave.screens.AnalyzerScreen

@Composable
fun AppNavHost(navController: NavHostController, innerPadding: androidx.compose.foundation.layout.PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = androidx.compose.ui.Modifier.padding(innerPadding)
    ) {
        composable("home") { HomeScreenContent(navController) }
        composable("settings") { SettingsScreenContent() }
        composable("analyzer") { AnalyzerScreen(navController) }
    }
}
