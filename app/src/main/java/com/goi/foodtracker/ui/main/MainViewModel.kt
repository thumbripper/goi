package com.goi.foodtracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goi.foodtracker.data.db.entities.CustomFoodItem
import com.goi.foodtracker.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val selectedFoods: Set<String> = emptySet(),
    val customText: String = "",
    val feelingScore: Int? = null,
    val saveCustom: Boolean = false,
    val saveSuccess: Boolean = false
)

val PRESET_FOODS = listOf(
    "Water" to "\uD83D\uDCA7",
    "Coffee" to "☕",
    "Tea" to "\uD83C\uDF75",
    "Alcohol" to "\uD83C\uDF7A",
    "Granola" to "\uD83C\uDF5E",
    "Bread/Toast" to "\uD83C\uDF5E",
    "Fruit" to "\uD83C\uDF4E",
    "Dairy" to "\uD83E\uDD5B"
)

val FEELING_OPTIONS = listOf(
    1 to "\uD83D\uDE0A",  // 😊
    2 to "\uD83D\uDE10",  // 😐
    3 to "\uD83E\uDD22",  // 🤢
    4 to "\uD83E\uDD2E"   // 🤮
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val customFoods: StateFlow<List<CustomFoodItem>> = logRepository.customFoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFood(name: String) {
        _uiState.update { state ->
            val updated = if (name in state.selectedFoods) {
                state.selectedFoods - name
            } else {
                state.selectedFoods + name
            }
            state.copy(selectedFoods = updated)
        }
    }

    fun setCustomText(text: String) {
        _uiState.update { it.copy(customText = text) }
    }

    fun setSaveCustom(save: Boolean) {
        _uiState.update { it.copy(saveCustom = save) }
    }

    fun setFeelingScore(score: Int) {
        _uiState.update { state ->
            state.copy(feelingScore = if (state.feelingScore == score) null else score)
        }
    }

    fun addCustomTextToSelection() {
        val text = _uiState.value.customText.trim()
        if (text.isBlank()) return
        _uiState.update { state ->
            state.copy(selectedFoods = state.selectedFoods + text, customText = "")
        }
    }

    fun saveEntry() {
        val state = _uiState.value
        if (state.selectedFoods.isEmpty() && state.feelingScore == null) return

        viewModelScope.launch {
            logRepository.saveEntry(
                foods = state.selectedFoods.toList(),
                feelingScore = state.feelingScore
            )
            if (state.saveCustom) {
                state.selectedFoods
                    .filter { food -> PRESET_FOODS.none { it.first == food } }
                    .forEach { logRepository.saveCustomFood(it) }
            }
            _uiState.value = MainUiState(saveSuccess = true)
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
