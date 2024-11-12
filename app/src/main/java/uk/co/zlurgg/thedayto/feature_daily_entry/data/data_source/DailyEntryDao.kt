package uk.co.zlurgg.thedayto.feature_daily_entry.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry

@Dao
interface DailyEntryDao {

    @Query("SELECT * FROM dailyentry")
    fun getDailyEntries(): Flow<List<DailyEntry>>

    @Query("SELECT * FROM dailyentry WHERE id = :id")
    fun getDailyEntryById(id: Int): DailyEntry?

    @Query("SELECT * FROM dailyentry WHERE dateStamp = :date")
    fun getDailyEntryByDate(date: Long): DailyEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyEntry(dailyEntry: DailyEntry)

    @Delete
    suspend fun deleteDailyEntry(dailyEntry: DailyEntry)

    @Update
    suspend fun updateDailyEntry(dailyEntry: DailyEntry)
}