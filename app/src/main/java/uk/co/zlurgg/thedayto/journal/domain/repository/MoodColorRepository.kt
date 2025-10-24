package uk.co.zlurgg.thedayto.journal.domain.repository

import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor

interface MoodColorRepository {
    suspend fun insertMoodColor(moodColor: MoodColor)
    suspend fun deleteMoodColor(moodColor: MoodColor)
    suspend fun getMoodColorById(id: Int): MoodColor?
    fun getMoodColors(): Flow<List<MoodColor>>
    suspend fun updateMoodColor(moodColor: MoodColor)
}