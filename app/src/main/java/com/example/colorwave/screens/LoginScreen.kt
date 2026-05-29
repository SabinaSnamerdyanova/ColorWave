package com.example.colorwave.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.colorwave.MainViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            "ColorWave",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    error = "Заполните все поля"
                    return@Button
                }

                loading = true

                viewModel.login(
                    email = email,
                    password = password,
                    onSuccess = {
                        loading = false
                        navController.navigate("main_app") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onError = {
                        loading = false
                        error = it
                    }
                )
            }
        ) {
            Text(if (loading) "Вход..." else "Войти")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            enabled = !loading,
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    error = "Заполните все поля для регистрации"
                    return@TextButton
                }

                loading = true

                viewModel.register(
                    email = email,
                    password = password,
                    onSuccess = {
                        loading = false
                        navController.navigate("main_app") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onError = { errorMessage ->
                        loading = false
                        error = errorMessage
                    }
                )
            }
        ) {
            Text(if (loading) "Регистрация..." else "Зарегистрироваться")
        }
    }
}
