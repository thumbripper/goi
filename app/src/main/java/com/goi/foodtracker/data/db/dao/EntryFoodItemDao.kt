package com.goi.foodtracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.goi.foodtracker.data.db.entities.EntryFoodItem
import kotlinx.coroutines.flow.Flow

data class FoodCorrelation(
    val foodName: String,
    val avgScore: Double,
    val entryCount: Int
)

data class FoodFrequency(
    val foodName: String,
    val count: Int
)

@Dao
interface EntryFoodItemDao {

    @Insert
    suspend fun insert(item: EntryFoodItem)

    @Insert
    suspend fun insertAll(items: List<EntryFoodItem>)

    @Query("SELECT * FROM entry_food_items WHERE entryId = :entryId")
    suspend fun getItemsForEntry(entryId: Long): List<EntryFoodItem>

    @Query("SELECT * FROM entry_food_items WHERE entryId IN (:entryIds)")
    fun getItemsForEntries(entryIds: List<Long>): Flow<List<EntryFoodItem>>

    /**
     * For each food item, find log_entries with a feeling_score that were recorded
     * within [windowMs] milliseconds after the food was logged. Average those scores.
     * Only include foods with at least [minEntries] data points.
     */
    @Query("""
        SELECT ef.foodName,
               AVG(le.feelingScore) AS avgScore,
               COUNT(DISTINCT le.id) AS entryCount
        FROM entry_food_items ef
        JOIN log_entries le
          ON le.feelingScore IS NOT NULL
         AND le.timestamp BETWEEN ef.loggedAt AND (ef.loggedAt + :windowMs)
        GROUP BY ef.foodName
        HAVING COUNT(DISTINCT le.id) >= :minEntries
        ORDER BY avgScore DESC
    """)
    fun getFoodCorrelations(windowMs: Long = 7_200_000L, minEntries: Int = 1): Flow<List<FoodCorrelation>>

    @Query("""
        SELECT foodName, COUNT(*) AS count
        FROM entry_food_items
        GROUP BY foodName
        ORDER BY count DESC
        LIMIT :limit
    """)
    fun getTopFoods(limit: Int = 10): Flow<List<FoodFrequency>>
}
