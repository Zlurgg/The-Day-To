package com.jbrightman.thedayto.feature_mood_color.data.repository

import com.jbrightman.thedayto.feature_mood_color.data.data_source.MoodColorDao
import com.jbrightman.thedayto.feature_mood_color.domain.model.MoodColor
import com.jbrightman.thedayto.feature_mood_color.domain.repository.MoodColorRepository
import kotlinx.coroutines.flow.Flow

class MoodColorRepositoryImpl (
    private val dao: MoodColorDao
    ): MoodColorRepository {
    override suspend fun insertMoodColor(moodColor: MoodColor) {
        return dao.insertMoodColor(moodColor)
    }

    override suspend fun deleteMoodColor(moodColor: MoodColor) {
        return dao.deleteMoodColor(moodColor)
    }

    override suspend fun getMoodColorById(id: Int): MoodColor? {
        return dao.getMoodColorById(id)
    }

    override fun getMoodColors(): Flow<List<MoodColor>> {
        return dao.getMoodColors()
    }

    override suspend fun updateMoodColor(moodColor: MoodColor) {
        return dao.updateEntry(moodColor)
    }
}