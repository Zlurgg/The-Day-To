package uk.co.zlurgg.thedayto.journal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.zlurgg.thedayto.journal.data.dao.MoodColorDao
import uk.co.zlurgg.thedayto.journal.data.mapper.toDomain
import uk.co.zlurgg.thedayto.journal.data.mapper.toEntity
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

class MoodColorRepositoryImpl(
    private val dao: MoodColorDao
) : MoodColorRepository {
    override suspend fun insertMoodColor(moodColor: MoodColor): Long {
        return dao.insertMoodColor(moodColor.toEntity())
    }

    override suspend fun deleteMoodColor(id: Int) {
        return dao.deleteMoodColor(id)
    }

    override suspend fun getMoodColorById(id: Int): MoodColor? {
        return dao.getMoodColorById(id)?.toDomain()
    }

    override suspend fun getMoodColorByName(mood: String): MoodColor? {
        return dao.getMoodColorByName(mood.trim().lowercase())?.toDomain()
    }

    override fun getMoodColors(): Flow<List<MoodColor>> {
        return dao.getMoodColors().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateMoodColor(moodColor: MoodColor) {
        return dao.updateMoodColor(moodColor.toEntity())
    }
}