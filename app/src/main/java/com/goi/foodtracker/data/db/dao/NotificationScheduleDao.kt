package com.goi.foodtracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.goi.foodtracker.data.db.entities.NotificationSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationScheduleDao {

    @Insert
    suspend fun insert(schedule: NotificationSchedule): Long

    @Update
    suspend fun update(schedule: NotificationSchedule)

    @Query("SELECT * FROM notification_schedules ORDER BY type, hour, minute")
    fun getAllSchedules(): Flow<List<NotificationSchedule>>

    @Query("SELECT * FROM notification_schedules WHERE enabled = 1")
    suspend fun getEnabledSchedules(): List<NotificationSchedule>

    @Query("DELETE FROM notification_schedules WHERE id = :id")
    suspend fun delete(id: Long)
}
