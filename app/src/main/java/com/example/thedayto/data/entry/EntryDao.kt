package com.example.thedayto.data.entry

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query("SELECT * from entries WHERE id = :id")
    fun getEntry(id: Int): Flow<Entry>

    /** Get all entries in date order **/
    @Query("SELECT * from entries ORDER BY date ASC")
    fun getAllEntries(): Flow<List<Entry>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: Entry)

    @Update
    suspend fun update(entry: Entry)

    @Delete
    suspend fun delete(entry: Entry)

    /** Get entry from date for use in calender **/
    @Query("SELECT * from entries WHERE date = :date")
    fun getEntryFromDate(date: String): Flow<Entry>

    @Query("DELETE FROM entries")
    suspend fun deleteAll()

}