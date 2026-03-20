package com.goi.foodtracker.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ScheduleType { FIXED, INTERVAL }

@Entity(tableName = "notification_schedules")
data class NotificationSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: ScheduleType,
    val hour: Int = 0,           // for FIXED: hour of day (0-23)
    val minute: Int = 0,         // for FIXED: minute (0-59)
    val intervalMinutes: Int = 120, // for INTERVAL: repeat interval
    val enabled: Boolean = true
)
