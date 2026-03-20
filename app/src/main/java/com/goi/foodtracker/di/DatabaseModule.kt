package com.goi.foodtracker.di

import android.content.Context
import androidx.room.Room
import com.goi.foodtracker.data.db.AppDatabase
import com.goi.foodtracker.data.db.dao.CustomFoodItemDao
import com.goi.foodtracker.data.db.dao.EntryFoodItemDao
import com.goi.foodtracker.data.db.dao.LogEntryDao
import com.goi.foodtracker.data.db.dao.NotificationScheduleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "food_tracker.db").build()

    @Provides fun provideLogEntryDao(db: AppDatabase): LogEntryDao = db.logEntryDao()
    @Provides fun provideEntryFoodItemDao(db: AppDatabase): EntryFoodItemDao = db.entryFoodItemDao()
    @Provides fun provideCustomFoodItemDao(db: AppDatabase): CustomFoodItemDao = db.customFoodItemDao()
    @Provides fun provideNotificationScheduleDao(db: AppDatabase): NotificationScheduleDao = db.notificationScheduleDao()
}
