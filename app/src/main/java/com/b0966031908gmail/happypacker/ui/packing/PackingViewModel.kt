package com.b0966031908gmail.happypacker.ui.packing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.b0966031908gmail.happypacker.utils.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PackingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PackingUiState>(PackingUiState.Loading)
    val uiState: StateFlow<PackingUiState> = _uiState

    fun loadArtworks(context: Context) {
        viewModelScope.launch {
            _uiState.value = PackingUiState.Loading

            val artworks = withContext(Dispatchers.IO) {
                FileHelper.getAllArtworks(context)
            }

            if (artworks.isEmpty()) {
                _uiState.value = PackingUiState.Empty
            } else {
                _uiState.value = PackingUiState.Success(artworks)
            }
        }
    }

    fun selectArtwork(file: File) {
        _uiState.value = PackingUiState.ArtworkSelected(file)
    }

    fun deleteArtwork(context: Context, file: File) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                FileHelper.deleteArtwork(file.absolutePath)
            }

            if (success) {
                // 重新載入作品列表
                loadArtworks(context)
            }
        }
    }
}

sealed class PackingUiState {
    object Loading : PackingUiState()
    object Empty : PackingUiState()
    data class Success(val artworks: List<File>) : PackingUiState()
    data class ArtworkSelected(val file: File) : PackingUiState()
}