package com.b0966031908gmail.happypacker.ui.canvas

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.b0966031908gmail.happypacker.utils.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CanvasViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CanvasUiState>(CanvasUiState.Drawing)
    val uiState: StateFlow<CanvasUiState> = _uiState

    private val _currentTool = MutableStateFlow(DrawingTool.PEN)
    val currentTool: StateFlow<DrawingTool> = _currentTool

    fun setTool(tool: DrawingTool) {
        _currentTool.value = tool
    }

    // 快速儲存，自動命名
    fun saveArtwork(context: Context, bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = CanvasUiState.Saving

            val result = withContext(Dispatchers.IO) {
                FileHelper.saveArtwork(context, bitmap)
            }

            if (result != null) {
                _uiState.value = CanvasUiState.SaveSuccess(result)
            } else {
                _uiState.value = CanvasUiState.SaveError("儲存失敗")
            }
        }
    }

    // 自訂檔名
    fun saveArtworkWithName(context: Context, bitmap: Bitmap, fileName: String) {
        viewModelScope.launch {
            _uiState.value = CanvasUiState.Saving

            val result = withContext(Dispatchers.IO) {
                FileHelper.saveArtworkWithName(context, bitmap, fileName)
            }

            if (result != null) {
                _uiState.value = CanvasUiState.SaveSuccess(result)
            } else {
                _uiState.value = CanvasUiState.SaveError("儲存失敗")
            }
        }
    }

    fun resetState() {
        _uiState.value = CanvasUiState.Drawing
    }
}

sealed class CanvasUiState {
    object Drawing : CanvasUiState()
    object Saving : CanvasUiState()
    data class SaveSuccess(val filePath: String) : CanvasUiState()
    data class SaveError(val message: String) : CanvasUiState()
}

enum class DrawingTool {
    PEN,
    ERASER
}