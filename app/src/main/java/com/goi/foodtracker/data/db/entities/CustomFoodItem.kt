package com.goi.foodtracker.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_food_items")
data class CustomFoodItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val useCount: Int = 1
)
