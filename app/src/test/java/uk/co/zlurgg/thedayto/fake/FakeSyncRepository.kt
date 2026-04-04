package uk.co.zlurgg.thedayto.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.sync.domain.model.SyncResult
import uk.co.zlurgg.thedayto.sync.domain.model.SyncState
import uk.co.zlurgg.thedayto.sync.domain.repository.SyncRepository

/**
 * Fake implementation of SyncRepository for testing.
 * Provides configurable behavior and call tracking for verification.
 */
class FakeSyncRepository : SyncRepository {

    // State management
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)

    // Configurable error behavior
    var shouldReturnError = false
    var syncError: DataError.Sync = DataError.Sync.UNKNOWN

    // Configurable sync result
    var syncResult = SyncResult()

    // Call tracking for verification
    var uploadPendingEntriesCalled = false
    var uploadPendingMoodColorsCalled = false
    var downloadEntriesCalled = false
    var downloadMoodColorsCalled = false
    var deleteEntryCalled = false
    var deleteMoodColorCalled = false
    var performFullSyncCalled = false
    var clearRemoteDataCalled = false
    var markLocalDataForSyncCalled = false
    var adoptOrphanedDataCalled = false
    var markSyncedAsLocalOnlyCalled = false
    var clearOtherUserDataCalled = false
    var clearUserDataCalled = false

    // Captured arguments for verification
    var lastUserId: String? = null
    var lastClearedUserId: String? = null

    // Configurable return values for methods that return counts
    var markLocalDataForSyncCount = 0
    var adoptOrphanedDataCount = 0
    var markSyncedAsLocalOnlyCount = 0
    var clearOtherUserDataCount = 0
    var clearUserDataCount = 0

    override fun observeSyncState(): Flow<SyncState> = _syncState.asStateFlow()

    override suspend fun uploadPendingEntries(
        entries: List<Entry>,
        userId: String
    ): Result<Int, DataError.Sync> {
        uploadPendingEntriesCalled = true
        lastUserId = userId
        return if (shouldReturnError) {
            Result.Error(syncError)
        } else {
            Result.Success(entries.size)
        }
    }

    override suspend fun uploadPendingMoodColors(
        moodColors: List<MoodColor>,
        userId: String
    ): Result<Int, DataError.Sync> {
        uploadPendingMoodColorsCalled = true
        lastUserId = userId
        return if (shouldReturnError) {
            Result.Error(syncError)
        } else {
            Result.Success(moodColors.size)
        }
    }

    override suspend fun downloadEntries(userId: String): Result<List<Entry>, DataError.Sync> {
        downloadEntriesCalled = true
        lastUserId = userId
        return if (shouldReturnError) {
            Result.Error(syncError)
        } else {
            Result.Success(emptyList())
        }
    }

    override suspend fun downloadMoodColors(userId: String): Result<List<MoodColor>, DataError.Sync> {
        downloadMoodColorsCalled = true
        lastUserId = userId
        return if (shouldReturnError) {
            Result.Error(syncError)
        } else {
            Result.Success(emptyList())
        }
    }

    override suspend fun deleteEntry(syncId: String, userId: String): EmptyResult<DataError.Sync> {
        deleteEntryCalled = true
        lastUserId = userId
        return if (shouldReturnError) {
            Result.Error(syncError)
        } else {
            Result.Success(Unit)
        }
    }

    override suspend fun deleteMoodColor(syncId: String, userId: String): EmptyResult<DataError.Sync> {
        deleteMoodColorCalled = true
        lastUserId = userId
        return if (shouldReturnError) {
            Result.Error(syncError)
        } else {
            Result.Success(Unit)
        }
    }

    override suspend fun performFullSync(userId: String): Result<SyncResult, DataError.Sync> {
        performFullSyncCalled = true
        lastUserId = userId
        return if (shouldReturnError) {
            _syncState.value = SyncState.Error(syncError)
            Result.Error(syncError)
        } else {
            _syncState.value = SyncState.Success(syncResult)
            Result.Success(syncResult)
        }
    }

    override suspend fun clearRemoteData(userId: String): EmptyResult<DataError.Sync> {
        clearRemoteDataCalled = true
        lastUserId = userId
        return if (shouldReturnError) {
            Result.Error(syncError)
        } else {
            Result.Success(Unit)
        }
    }

    override suspend fun markLocalDataForSync(): Int {
        markLocalDataForSyncCalled = true
        return markLocalDataForSyncCount
    }

    override suspend fun adoptOrphanedData(userId: String): Int {
        adoptOrphanedDataCalled = true
        lastUserId = userId
        return adoptOrphanedDataCount
    }

    override suspend fun markSyncedAsLocalOnly(): Int {
        markSyncedAsLocalOnlyCalled = true
        return markSyncedAsLocalOnlyCount
    }

    override suspend fun clearOtherUserData(currentUserId: String): Int {
        clearOtherUserDataCalled = true
        lastUserId = currentUserId
        return clearOtherUserDataCount
    }

    override suspend fun clearUserData(userId: String): Int {
        clearUserDataCalled = true
        lastClearedUserId = userId
        return clearUserDataCount
    }

    /**
     * Helper to emit a specific sync state for testing.
     */
    fun emitSyncState(state: SyncState) {
        _syncState.value = state
    }

    /**
     * Helper to reset all values to defaults.
     */
    fun reset() {
        _syncState.value = SyncState.Idle
        shouldReturnError = false
        syncError = DataError.Sync.UNKNOWN
        syncResult = SyncResult()

        uploadPendingEntriesCalled = false
        uploadPendingMoodColorsCalled = false
        downloadEntriesCalled = false
        downloadMoodColorsCalled = false
        deleteEntryCalled = false
        deleteMoodColorCalled = false
        performFullSyncCalled = false
        clearRemoteDataCalled = false
        markLocalDataForSyncCalled = false
        adoptOrphanedDataCalled = false
        markSyncedAsLocalOnlyCalled = false
        clearOtherUserDataCalled = false
        clearUserDataCalled = false

        lastUserId = null
        lastClearedUserId = null

        markLocalDataForSyncCount = 0
        adoptOrphanedDataCount = 0
        markSyncedAsLocalOnlyCount = 0
        clearOtherUserDataCount = 0
        clearUserDataCount = 0
    }
}
