package com.example.thedayto.data.entries

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

    @Query("SELECT * from entries WHERE id = :id")
    fun getEntry(id: Int): Flow<Entry>

    @Query("SELECT * from entries ORDER BY date ASC")
    fun getAllEntries(): Flow<List<Entry>>

    /* method to get mood from date to populate table, for each day in month of year get mood */
}