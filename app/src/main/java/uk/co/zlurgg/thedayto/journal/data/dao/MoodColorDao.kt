package uk.co.zlurgg.thedayto.journal.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.journal.data.model.MoodColorEntity

@Dao
interface MoodColorDao {
    @Query("SELECT * FROM mood_color WHERE isDeleted = 0")
    fun getMoodColors(): Flow<List<MoodColorEntity>>

    @Query("SELECT * FROM mood_color WHERE id = :id")
    suspend fun getMoodColorById(id: Int): MoodColorEntity?

    @Query("SELECT * FROM mood_color WHERE moodNormalized = :moodNormalized")
    suspend fun getMoodColorByName(moodNormalized: String): MoodColorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodColor(moodColor: MoodColorEntity)

    @Query("UPDATE mood_color SET isDeleted = 1 WHERE id = :id")
    suspend fun deleteMoodColor(id: Int)

    @Update
    suspend fun updateMoodColor(moodColor: MoodColorEntity)
}