package com.example.colorwave

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.colorwave.data.AppDatabase
import com.example.colorwave.data.PaletteEntity
import com.example.colorwave.data.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.appDao()

    var currentUser by mutableStateOf<String?>(null)
        private set

    private val _userPalettes = MutableStateFlow<List<PaletteEntity>>(emptyList())
    val userPalettes: StateFlow<List<PaletteEntity>> = _userPalettes

    var lastAnalysisResult by mutableStateOf<List<Color>?>(null)
    var lastTrackTitle by mutableStateOf("")

    fun login(login: String) {
        currentUser = login
        viewModelScope.launch {
            dao.registerUser(UserEntity(login, ""))
            observePalettes()
        }
    }

    private fun observePalettes() {
        val login = currentUser ?: return
        viewModelScope.launch {
            dao.getPalettesForUser(login).collect {
                _userPalettes.value = it
            }
        }
    }

    fun savePalette(trackName: String, colors: List<Color>) {
        val login = currentUser ?: return
        val hexString = colors.joinToString(",") { 
            String.format("#%06X", (0xFFFFFF and it.toArgb())) 
        }
        viewModelScope.launch {
            dao.insertPalette(PaletteEntity(userLogin = login, trackName = trackName, colorsHex = hexString))
        }
    }

    fun deletePalette(palette: PaletteEntity) {
        viewModelScope.launch {
            dao.deletePalette(palette)
        }
    }

    fun logout() {
        currentUser = null
        _userPalettes.value = emptyList()
        lastAnalysisResult = null
    }
}
