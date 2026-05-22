package com.example.colorwave.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.colorwave.AppNavHost
import com.example.colorwave.MainViewModel

@Composable
fun MainScreen(rootNavController: NavHostController, mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == "home" || currentRoute == "collection" || currentRoute == "settings") {
                Surface(modifier = Modifier.shadow(8.dp), tonalElevation = 8.dp) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, null) },
                            label = { Text("Главная") },
                            selected = currentRoute == "home",
                            onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Collections, null) },
                            label = { Text("Коллекция") },
                            selected = currentRoute == "collection",
                            onClick = { navController.navigate("collection") { popUpTo("home") } }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Settings, null) },
                            label = { Text("Настройки") },
                            selected = currentRoute == "settings",
                            onClick = { navController.navigate("settings") { popUpTo("home") } }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            rootNavController = rootNavController,
            mainViewModel = mainViewModel,
            innerPadding = innerPadding
        )
    }
}
