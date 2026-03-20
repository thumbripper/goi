package com.goi.foodtracker.data.repository

import com.goi.foodtracker.data.db.dao.NotificationScheduleDao
import com.goi.foodtracker.data.db.entities.NotificationSchedule
import com.goi.foodtracker.data.db.entities.ScheduleType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val dao: NotificationScheduleDao
) {
    val allSchedules: Flow<List<NotificationSchedule>> = dao.getAllSchedules()

    suspend fun getEnabledSchedules(): List<NotificationSchedule> = dao.getEnabledSchedules()

    suspend fun addFixedSchedule(hour: Int, minute: Int): Long =
        dao.insert(NotificationSchedule(type = ScheduleType.FIXED, hour = hour, minute = minute))

    suspend fun addIntervalSchedule(intervalMinutes: Int): Long =
        dao.insert(NotificationSchedule(type = ScheduleType.INTERVAL, intervalMinutes = intervalMinutes))

    suspend fun updateSchedule(schedule: NotificationSchedule) = dao.update(schedule)

    suspend fun deleteSchedule(id: Long) = dao.delete(id)
}
