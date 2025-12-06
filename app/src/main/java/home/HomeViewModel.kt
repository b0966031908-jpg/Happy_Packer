package com.yourpackage.happypacker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun onButtonClick(button: HomeButton) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.NavigateTo(button)
        }
    }

    fun resetState() {
        _uiState.value = HomeUiState.Idle
    }
}

sealed class HomeUiState {
    object Idle : HomeUiState()
    data class NavigateTo(val button: HomeButton) : HomeUiState()
}

enum class HomeButton {
    CANVAS,
    PACKING,
    SELLING
}