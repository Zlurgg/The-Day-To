package com.jbrightman.thedayto.feature_thedayto.domain.repository.mood_color

import com.jbrightman.thedayto.feature_thedayto.domain.model.entry.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.model.mood_color.MoodColor
import kotlinx.coroutines.flow.Flow

interface MoodColorRepository {
    suspend fun insertMoodColor(moodColor: MoodColor)
    suspend fun deleteMoodColor(moodColor: MoodColor)
    suspend fun getMoodColorById(id: Int): MoodColor?
    fun getMoodColors(): Flow<List<MoodColor>>
    suspend fun updateMoodColor(moodColor: MoodColor)
}