package uk.co.zlurgg.thedayto.journal.domain.repository

import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor

interface MoodColorRepository {
    suspend fun insertMoodColor(moodColor: MoodColor)
    suspend fun deleteMoodColor(id: Int)
    suspend fun getMoodColorById(id: Int): MoodColor?
    suspend fun getMoodColorByName(mood: String): MoodColor?
    fun getMoodColors(): Flow<List<MoodColor>>
    suspend fun updateMoodColor(moodColor: MoodColor)
}