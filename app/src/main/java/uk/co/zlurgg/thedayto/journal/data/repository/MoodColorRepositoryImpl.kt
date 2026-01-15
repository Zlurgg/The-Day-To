package uk.co.zlurgg.thedayto.journal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.error.ErrorMapper
import io.github.zlurgg.core.domain.result.EmptyResult
import io.github.zlurgg.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.data.dao.MoodColorDao
import uk.co.zlurgg.thedayto.journal.data.mapper.toDomain
import uk.co.zlurgg.thedayto.journal.data.mapper.toEntity
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

class MoodColorRepositoryImpl(
    private val dao: MoodColorDao
) : MoodColorRepository {

    override suspend fun insertMoodColor(moodColor: MoodColor): Result<Long, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.insertMoodColor(moodColor.toEntity())
        }
    }

    override suspend fun deleteMoodColor(id: Int): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.deleteMoodColor(id)
        }
    }

    override suspend fun getMoodColorById(id: Int): Result<MoodColor?, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getMoodColorById(id)?.toDomain()
        }
    }

    override suspend fun getMoodColorByName(mood: String): Result<MoodColor?, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getMoodColorByName(mood.trim().lowercase())?.toDomain()
        }
    }

    override fun getMoodColors(): Flow<List<MoodColor>> {
        return dao.getMoodColors().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateMoodColor(moodColor: MoodColor): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.updateMoodColor(moodColor.toEntity())
        }
    }

    companion object {
        private const val TAG = "MoodColorRepository"
    }
}