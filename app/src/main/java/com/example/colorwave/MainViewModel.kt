package com.example.colorwave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.colorwave.auth.AuthRepository
import com.example.colorwave.data.FirebasePalette
import com.example.colorwave.data.FirebasePaletteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val paletteRepository = FirebasePaletteRepository()

    var currentUser by mutableStateOf(authRepository.currentUser())
        private set

    private val _userPalettes = MutableStateFlow<List<FirebasePalette>>(emptyList())
    val userPalettes: StateFlow<List<FirebasePalette>> = _userPalettes

    var lastAnalysisResult by mutableStateOf<List<Color>?>(null)
    var lastTrackTitle by mutableStateOf("")

    private var palettesJob: kotlinx.coroutines.Job? = null

    init {
        if (currentUser != null) {
            startObservingPalettes()
        }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authRepository.signIn(email, password) { success, error ->
            if (success) {
                currentUser = authRepository.currentUser()
                startObservingPalettes()
                onSuccess()
            } else {
                onError(error ?: "Ошибка входа")
            }
        }
    }

    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authRepository.signUp(email, password) { success, error ->
            if (success) {
                currentUser = authRepository.currentUser()
                startObservingPalettes()
                onSuccess()
            } else {
                onError(error ?: "Ошибка регистрации")
            }
        }
    }

    fun logout() {
        palettesJob?.cancel()
        authRepository.logout()
        currentUser = null
        _userPalettes.value = emptyList()
        lastAnalysisResult = null
        lastTrackTitle = ""
    }

    private fun startObservingPalettes() {
        palettesJob?.cancel()
        palettesJob = viewModelScope.launch {
            paletteRepository.observePalettes().collect {
                _userPalettes.value = it
            }
        }
    }

    fun savePalette(trackName: String, colors: List<Color>) {
        val hexColors = colors.map {
            String.format("#%06X", 0xFFFFFF and it.toArgb())
        }
        paletteRepository.savePalette(trackName, hexColors)
    }

    fun deletePalette(paletteId: String) {
        paletteRepository.deletePalette(paletteId)
    }
}