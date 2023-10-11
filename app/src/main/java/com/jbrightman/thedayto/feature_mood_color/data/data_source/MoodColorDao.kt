package com.jbrightman.thedayto.feature_mood_color.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jbrightman.thedayto.feature_mood_color.domain.model.MoodColor
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodColorDao {
    @Query("SELECT * FROM moodColor")
    fun getMoodColors(): Flow<List<MoodColor>>
    @Query("SELECT * FROM moodColor WHERE id = :id")
    suspend fun getMoodColorById(id: Int): MoodColor?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodColor(moodColor: MoodColor)
    @Delete
    suspend fun deleteMoodColor(moodColor: MoodColor)

    @Update
    suspend fun updateEntry(moodColor: MoodColor)
}