package com.jbrightman.thedayto.feature_thedayto.data.repository.mood_color

import com.jbrightman.thedayto.feature_thedayto.data.data_source.mood_color.MoodColorDao
import com.jbrightman.thedayto.feature_thedayto.domain.model.entry.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.model.mood_color.MoodColor
import com.jbrightman.thedayto.feature_thedayto.domain.repository.mood_color.MoodColorRepository
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