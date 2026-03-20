package com.goi.foodtracker.data.repository

import com.goi.foodtracker.data.db.dao.CustomFoodItemDao
import com.goi.foodtracker.data.db.dao.EntryFoodItemDao
import com.goi.foodtracker.data.db.dao.FoodCorrelation
import com.goi.foodtracker.data.db.dao.FoodFrequency
import com.goi.foodtracker.data.db.dao.LogEntryDao
import com.goi.foodtracker.data.db.entities.CustomFoodItem
import com.goi.foodtracker.data.db.entities.EntryFoodItem
import com.goi.foodtracker.data.db.entities.LogEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(
    private val logEntryDao: LogEntryDao,
    private val entryFoodItemDao: EntryFoodItemDao,
    private val customFoodItemDao: CustomFoodItemDao
) {
    val allEntries: Flow<List<LogEntry>> = logEntryDao.getAllEntries()
    val customFoods: Flow<List<CustomFoodItem>> = customFoodItemDao.getAllItems()
    val averageFeelingScore: Flow<Double?> = logEntryDao.getAverageFeelingScore()

    suspend fun saveEntry(
        foods: List<String>,
        feelingScore: Int?,
        timestamp: Long = System.currentTimeMillis()
    ): Long {
        val entryId = logEntryDao.insert(LogEntry(timestamp = timestamp, feelingScore = feelingScore))
        val items = foods.map { EntryFoodItem(entryId = entryId, foodName = it, loggedAt = timestamp) }
        if (items.isNotEmpty()) entryFoodItemDao.insertAll(items)
        return entryId
    }

    suspend fun saveFeelingOnly(feelingScore: Int) {
        logEntryDao.insert(LogEntry(feelingScore = feelingScore))
    }

    fun getFoodItemsForEntries(entryIds: List<Long>): Flow<List<EntryFoodItem>> =
        entryFoodItemDao.getItemsForEntries(entryIds)

    fun getFoodCorrelations(): Flow<List<FoodCorrelation>> =
        entryFoodItemDao.getFoodCorrelations(windowMs = 7_200_000L, minEntries = 1)

    fun getTopFoods(limit: Int = 10): Flow<List<FoodFrequency>> =
        entryFoodItemDao.getTopFoods(limit)

    suspend fun saveCustomFood(name: String) {
        if (customFoodItemDao.countByName(name) == 0) {
            customFoodItemDao.insert(CustomFoodItem(name = name))
        } else {
            customFoodItemDao.incrementUseCount(name)
        }
    }

    suspend fun deleteCustomFood(id: Long) {
        customFoodItemDao.delete(id)
    }

    suspend fun deleteEntry(id: Long) {
        logEntryDao.delete(id)
    }
}
