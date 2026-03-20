package com.goi.foodtracker.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entry_food_items",
    foreignKeys = [ForeignKey(
        entity = LogEntry::class,
        parentColumns = ["id"],
        childColumns = ["entryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("entryId")]
)
data class EntryFoodItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val foodName: String,
    val loggedAt: Long = System.currentTimeMillis()
)
