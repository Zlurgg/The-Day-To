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
    private val preferencesRepository: PreferencesRepository,
) : MoodColorRepository {

    override suspend fun insertMoodColor(moodColor: MoodColor): Result<Long, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            val syncEnabled = preferencesRepository.isSyncEnabled()
            val moodColorWithSync = moodColor.copy(
                syncId = moodColor.syncId ?: UUID.randomUUID().toString(),
                updatedAt = moodColor.updatedAt ?: System.currentTimeMillis(),
                syncStatus = if (syncEnabled) SyncStatus.PENDING_SYNC else SyncStatus.LOCAL_ONLY,
            )
            dao.insertMoodColor(moodColorWithSync.toEntity())
        }
    }

    override suspend fun deleteMoodColor(id: Int): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            // Wrap the PENDING_DELETE write and the isDeleted flip in one transaction
            // so another reader can't catch the row in a half-deleted state.
            dao.softDeleteWithSync(
                id = id,
                markPendingDelete = preferencesRepository.isSyncEnabled(),
            )
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

    override suspend fun getActiveCount(): Result<Int, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getActiveCount()
        }
    }

    // Intentionally bare (no ErrorMapper / Result wrapper). This is a non-critical
    // read used only by the random seeder — if the DAO throws, the exception
    // propagates to the ViewModel's coroutine which has a finally block that
    // re-enables the dice button. Adding a Result wrapper would add boilerplate
    // for no user-visible gain.
    override suspend fun getActiveMoodNames(): Set<String> {
        return dao.getActiveMoodNames().toSet()
    }

    override fun getMoodColors(): Flow<List<MoodColor>> {
        return dao.getMoodColors().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateMoodColor(moodColor: MoodColor): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            val syncEnabled = preferencesRepository.isSyncEnabled()

            // Preserve existing sync fields when updating
            val existingMoodColor = moodColor.id?.let { dao.getMoodColorById(it) }

            val moodColorWithSync = moodColor.copy(
                syncId = moodColor.syncId ?: existingMoodColor?.syncId ?: UUID.randomUUID().toString(),
                userId = moodColor.userId ?: existingMoodColor?.userId,
                updatedAt = System.currentTimeMillis(),
                syncStatus = if (syncEnabled) SyncStatus.PENDING_SYNC else moodColor.syncStatus,
            )
            dao.updateMoodColor(moodColorWithSync.toEntity())
        }
    }

    override suspend fun setFavorite(id: Int, isFavorite: Boolean): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.updateFavorite(id, isFavorite, System.currentTimeMillis())
        }
    }

    override suspend fun restore(id: Int): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.restore(id, System.currentTimeMillis())
        }
    }

    companion object {
        private const val TAG = "MoodColorRepository"
    }
}
