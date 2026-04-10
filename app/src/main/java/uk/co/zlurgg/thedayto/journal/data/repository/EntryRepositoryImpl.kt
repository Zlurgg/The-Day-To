package uk.co.zlurgg.thedayto.journal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.error.ErrorMapper
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.data.dao.EntryDao
import uk.co.zlurgg.thedayto.journal.data.mapper.toDomain
import uk.co.zlurgg.thedayto.journal.data.mapper.toEntity
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
import uk.co.zlurgg.thedayto.sync.data.dao.PendingSyncDeletionDao
import uk.co.zlurgg.thedayto.sync.data.model.PendingSyncDeletionEntity
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class EntryRepositoryImpl(
    private val dao: EntryDao,
    private val preferencesRepository: PreferencesRepository,
    private val pendingSyncDeletionDao: PendingSyncDeletionDao,
) : EntryRepository {
    override fun getEntries(): Flow<List<Entry>> {
        return dao.getEntries().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEntriesWithMoodColors(): Flow<List<EntryWithMoodColor>> {
        return dao.getEntriesWithMoodColors().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEntriesForMonth(month: Int, year: Int): Flow<List<EntryWithMoodColor>> {
        require(month in 1..12) { "Month must be between 1 and 12, got: $month" }
        require(year > 0) { "Year must be positive, got: $year" }

        val (startEpoch, endEpoch) = getMonthRange(month, year)

        return dao.getEntriesForMonth(startEpoch, endEpoch).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Calculate the epoch second range for a given month.
     *
     * @param month Month value (1-12)
     * @param year Year value
     * @return Pair of (startEpoch, endEpoch) where start is inclusive and end is exclusive
     */
    private fun getMonthRange(month: Int, year: Int): Pair<Long, Long> {
        val startOfMonth = LocalDate.of(year, month, 1)
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC)

        val startOfNextMonth = LocalDate.of(year, month, 1)
            .plusMonths(1)
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC)

        return Pair(startOfMonth, startOfNextMonth)
    }

    override suspend fun getEntryById(id: Int): Result<Entry?, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getEntryById(id)?.toDomain()
        }
    }

    override suspend fun getEntryWithMoodColorById(id: Int): Result<EntryWithMoodColor?, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getEntryWithMoodColorById(id)?.toDomain()
        }
    }

    override suspend fun getEntryByDate(date: Long): Result<Entry?, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getEntryByDate(date)?.toDomain()
        }
    }

    override suspend fun getEntryWithMoodColorByDate(date: Long): Result<EntryWithMoodColor?, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getEntryWithMoodColorByDate(date)?.toDomain()
        }
    }

    override suspend fun insertEntry(entry: Entry): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            val syncEnabled = preferencesRepository.isSyncEnabled()

            // Preserve existing sync fields when updating (entry.id != null)
            val existingEntry = entry.id?.let { dao.getEntryById(it) }

            val entryWithSync = entry.copy(
                syncId = entry.syncId ?: existingEntry?.syncId ?: UUID.randomUUID().toString(),
                userId = entry.userId ?: existingEntry?.userId,
                updatedAt = System.currentTimeMillis(),
                syncStatus = if (syncEnabled) SyncStatus.PENDING_SYNC else SyncStatus.LOCAL_ONLY,
            )
            dao.insertEntry(entryWithSync.toEntity())
        }
    }

    override suspend fun deleteEntry(id: Int): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            val syncEnabled = preferencesRepository.isSyncEnabled()
            val entry = dao.getEntryById(id)

            // If entry was synced, track deletion for Firestore sync
            if (syncEnabled && entry?.syncId != null && entry.userId != null) {
                pendingSyncDeletionDao.insert(
                    PendingSyncDeletionEntity(
                        syncId = entry.syncId,
                        collection = PendingSyncDeletionEntity.COLLECTION_ENTRIES,
                        userId = entry.userId,
                    ),
                )
            }
            // Hard delete immediately - entry is gone from local DB
            dao.deleteEntry(id)
        }
    }

    override suspend fun updateEntry(entry: Entry): EmptyResult<DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            val syncEnabled = preferencesRepository.isSyncEnabled()

            // Preserve existing syncId when updating
            val existingEntry = entry.id?.let { dao.getEntryById(it) }

            val entryWithSync = entry.copy(
                syncId = entry.syncId ?: existingEntry?.syncId ?: UUID.randomUUID().toString(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = if (syncEnabled) SyncStatus.PENDING_SYNC else entry.syncStatus,
            )
            dao.updateEntry(entryWithSync.toEntity())
        }
    }

    companion object {
        private const val TAG = "EntryRepository"
    }

    override fun getMoodColorEntryCounts(): Flow<Map<Int, Int>> {
        return dao.getMoodColorEntryCounts().map { counts ->
            counts.associate { it.moodColorId to it.entryCount }
        }
    }
}
