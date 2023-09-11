package com.example.thedayto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
@Dao
interface TheDayToDao {

    @Query("SELECT * FROM entries_table WHERE date = :date")
    fun getEntryFromDate(date: String): TheDayToEntity

    @Query("SELECT * FROM entries_table ORDER BY date ASC")
    fun getEntriesOrderedByDate(): Flow<List<TheDayToEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(theDayToEntity: TheDayToEntity)

    @Query("DELETE FROM entries_table")
    suspend fun deleteAll()
}