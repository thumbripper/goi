package com.goi.foodtracker.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goi.foodtracker.data.db.dao.FoodCorrelation
import com.goi.foodtracker.data.db.dao.FoodFrequency
import com.goi.foodtracker.data.db.entities.EntryFoodItem
import com.goi.foodtracker.data.db.entities.LogEntry
import com.goi.foodtracker.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TimelineEntry(
    val entry: LogEntry,
    val foods: List<EntryFoodItem>
)

data class ReportsUiState(
    val timeline: List<TimelineEntry> = emptyList(),
    val correlations: List<FoodCorrelation> = emptyList(),
    val topFoods: List<FoodFrequency> = emptyList(),
    val avgFeelingScore: Double? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {

    val uiState: StateFlow<ReportsUiState> = combine(
        logRepository.allEntries,
        logRepository.getFoodCorrelations(),
        logRepository.getTopFoods(),
        logRepository.averageFeelingScore
    ) { entries, correlations, topFoods, avgScore ->
        val entryIds = entries.map { it.id }
        ReportsUiState(
            timeline = entries.map { entry ->
                TimelineEntry(entry = entry, foods = emptyList())
            },
            correlations = correlations,
            topFoods = topFoods,
            avgFeelingScore = avgScore
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportsUiState()
    )

    // Separate flow for food items mapped to entries
    val foodItemsFlow = combine(
        logRepository.allEntries,
        logRepository.allEntries
    ) { entries, _ ->
        entries
    }
}
