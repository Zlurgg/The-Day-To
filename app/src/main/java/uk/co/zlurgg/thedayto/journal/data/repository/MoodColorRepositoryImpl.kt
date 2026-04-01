package uk.co.zlurgg.thedayto.journal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.error.ErrorMapper
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.data.dao.MoodColorDao
import uk.co.zlurgg.thedayto.journal.data.mapper.toDomain
import uk.co.zlurgg.thedayto.journal.data.mapper.toEntity
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus
import java.util.UUID

class MoodColorRepositoryImpl(
    private val dao: MoodColorDao,
    private val preferencesRepository: PreferencesRepository
) : MoodColorRepository {

    override suspend fun insertMoodColor(moodColor: MoodColor): Result<Long, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            val syncEnabled = preferencesRepository.isSyncEnabled()
            val moodColorWithSync = moodColor.copy(
                syncId = moodColor.syncId ?: UUID.randomUUID().toString(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = if (syncEnabled) SyncStatus.PENDING_SYNC else SyncStatus.LOCAL_ONLY
            )
            dao.insertMoodColor(moodColorWithSync.toEntity())
        }
    }

    override suspend fun deleteMoodColor(id: Int): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            val syncEnabled = preferencesRepository.isSyncEnabled()
            val moodColor = dao.getMoodColorById(id)
            if (syncEnabled && moodColor?.syncId != null) {
                // Mark for sync deletion (soft delete with PENDING_DELETE status)
                dao.updateSyncStatus(id, SyncStatus.PENDING_DELETE.name)
            }
            // Always soft-delete locally
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
            val syncEnabled = preferencesRepository.isSyncEnabled()
            val moodColorWithSync = moodColor.copy(
                syncId = moodColor.syncId ?: UUID.randomUUID().toString(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = if (syncEnabled) SyncStatus.PENDING_SYNC else moodColor.syncStatus
            )
            dao.updateMoodColor(moodColorWithSync.toEntity())
        }
    }

    companion object {
        private const val TAG = "MoodColorRepository"
    }
}