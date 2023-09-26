package com.jbrightman.thedayto.feature_thedayto.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface TheDayToDao {

    @Query("SELECT * FROM thedaytoentry")
    fun getTheDayToEntries(): Flow<List<TheDayToEntry>>

    @Query("SELECT * FROM thedaytoentry WHERE id = :id")
    fun getTheDayToEntryById(id: Int): TheDayToEntry?

    @Query("SELECT * FROM thedaytoentry WHERE dateStamp = :date")
    fun getTheDayToEntryByDate(date: Long): TheDayToEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(theDayToEntry: TheDayToEntry)

    @Delete
    suspend fun deleteEntry(theDayToEntry: TheDayToEntry)
    @Update
    suspend fun updateEntry(theDayToEntry: TheDayToEntry)
}