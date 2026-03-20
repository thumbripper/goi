package com.goi.foodtracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.goi.foodtracker.data.db.entities.LogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEntryDao {

    @Insert
    suspend fun insert(entry: LogEntry): Long

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries WHERE timestamp >= :fromTime ORDER BY timestamp DESC")
    fun getEntriesSince(fromTime: Long): Flow<List<LogEntry>>

    @Query("SELECT AVG(feelingScore) FROM log_entries WHERE feelingScore IS NOT NULL")
    fun getAverageFeelingScore(): Flow<Double?>

    @Query("DELETE FROM log_entries WHERE id = :id")
    suspend fun delete(id: Long)
}
