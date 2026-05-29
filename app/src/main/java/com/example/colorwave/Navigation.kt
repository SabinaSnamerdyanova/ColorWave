package com.example.colorwave

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.colorwave.screens.*

@Composable
fun AppNavHost(
    navController: NavHostController,
    rootNavController: NavHostController,
    mainViewModel: MainViewModel,
    innerPadding: androidx.compose.foundation.layout.PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = androidx.compose.ui.Modifier.padding(innerPadding)
    ) {
        composable("home") { HomeScreenContent(navController) }

        composable("collection") {
            CollectionScreen(viewModel = mainViewModel)
        }

        composable("settings") {
            SettingsScreenContent(rootNavController = rootNavController)
        }

        composable("analyzer") { AnalyzerScreen(navController) }

        composable(
            route = "music_result/{fileUri}",
            arguments = listOf(navArgument("fileUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileUri = backStackEntry.arguments?.getString("fileUri") ?: ""
            MusicResultScreen(navController, fileUri, mainViewModel)
        }
    }
}
