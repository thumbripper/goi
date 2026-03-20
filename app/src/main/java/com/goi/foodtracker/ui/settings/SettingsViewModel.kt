package com.goi.foodtracker.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goi.foodtracker.data.db.entities.CustomFoodItem
import com.goi.foodtracker.data.db.entities.NotificationSchedule
import com.goi.foodtracker.data.db.entities.ScheduleType
import com.goi.foodtracker.data.repository.LogRepository
import com.goi.foodtracker.data.repository.NotificationRepository
import com.goi.foodtracker.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val logRepository: LogRepository,
    private val notificationScheduler: NotificationScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val schedules: StateFlow<List<NotificationSchedule>> = notificationRepository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customFoods: StateFlow<List<CustomFoodItem>> = logRepository.customFoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFixedSchedule(hour: Int, minute: Int) {
        viewModelScope.launch {
            notificationRepository.addFixedSchedule(hour, minute)
            notificationScheduler.rescheduleAll()
        }
    }

    fun addIntervalSchedule(intervalMinutes: Int) {
        viewModelScope.launch {
            // Remove existing interval schedules first (only one interval allowed)
            schedules.value
                .filter { it.type == ScheduleType.INTERVAL }
                .forEach { notificationRepository.deleteSchedule(it.id) }
            notificationRepository.addIntervalSchedule(intervalMinutes)
            notificationScheduler.rescheduleAll()
        }
    }

    fun toggleSchedule(schedule: NotificationSchedule) {
        viewModelScope.launch {
            notificationRepository.updateSchedule(schedule.copy(enabled = !schedule.enabled))
            notificationScheduler.rescheduleAll()
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            notificationRepository.deleteSchedule(id)
            notificationScheduler.rescheduleAll()
        }
    }

    fun deleteCustomFood(id: Long) {
        viewModelScope.launch {
            logRepository.deleteCustomFood(id)
        }
    }
}
