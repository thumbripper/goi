package com.goi.foodtracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.goi.foodtracker.data.db.dao.CustomFoodItemDao
import com.goi.foodtracker.data.db.dao.EntryFoodItemDao
import com.goi.foodtracker.data.db.dao.LogEntryDao
import com.goi.foodtracker.data.db.dao.NotificationScheduleDao
import com.goi.foodtracker.data.db.entities.CustomFoodItem
import com.goi.foodtracker.data.db.entities.EntryFoodItem
import com.goi.foodtracker.data.db.entities.LogEntry
import com.goi.foodtracker.data.db.entities.NotificationSchedule

@Database(
    entities = [
        LogEntry::class,
        EntryFoodItem::class,
        CustomFoodItem::class,
        NotificationSchedule::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logEntryDao(): LogEntryDao
    abstract fun entryFoodItemDao(): EntryFoodItemDao
    abstract fun customFoodItemDao(): CustomFoodItemDao
    abstract fun notificationScheduleDao(): NotificationScheduleDao
}
