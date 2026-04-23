package com.example.colorwave.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HomeScreenContent(navController: NavHostController) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            navController.navigate("analyzer")
        } else {
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ColorWave", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(20.dp))

            FloatingActionButton(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Запись")
            }
        }
    }
}