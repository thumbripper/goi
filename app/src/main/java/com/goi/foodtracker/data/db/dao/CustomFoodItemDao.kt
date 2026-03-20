package com.goi.foodtracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.goi.foodtracker.data.db.entities.CustomFoodItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomFoodItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: CustomFoodItem): Long

    @Query("UPDATE custom_food_items SET useCount = useCount + 1 WHERE name = :name")
    suspend fun incrementUseCount(name: String)

    @Query("SELECT * FROM custom_food_items ORDER BY useCount DESC")
    fun getAllItems(): Flow<List<CustomFoodItem>>

    @Query("DELETE FROM custom_food_items WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM custom_food_items WHERE name = :name")
    suspend fun countByName(name: String): Int
}
