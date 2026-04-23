package com.example.colorwave

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.colorwave.screens.HomeScreenContent
import com.example.colorwave.screens.SettingsScreenContent

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

@Composable
fun AnalyzerScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Экран анализа звука (Пусто)")
    }
}