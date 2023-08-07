package com.example.thedayto.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: Entry)

    @Update
    suspend fun update(entry: Entry)

    @Delete
    suspend fun delete(entry: Entry)

    @Query("SELECT * from daily_entries WHERE id = :id")
    fun getDailyEntry(id: Int): Flow<Entry>

    @Query("SELECT * from daily_entries ORDER BY date ASC")
    fun getAllDailyEntries(): Flow<List<Entry>>

    /* method to get mood from date to populate table, for each day in month of year get mood */
}