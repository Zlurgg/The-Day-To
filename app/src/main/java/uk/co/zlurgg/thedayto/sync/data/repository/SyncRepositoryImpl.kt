package uk.co.zlurgg.thedayto.sync.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.data.dao.EntryDao
import uk.co.zlurgg.thedayto.journal.data.dao.MoodColorDao
import uk.co.zlurgg.thedayto.sync.data.dao.PendingSyncDeletionDao
import uk.co.zlurgg.thedayto.sync.data.model.PendingSyncDeletionEntity
import uk.co.zlurgg.thedayto.journal.data.mapper.toDomain
import uk.co.zlurgg.thedayto.journal.data.mapper.toEntity
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.notification.domain.sync.NotificationSyncService
import uk.co.zlurgg.thedayto.sync.data.mapper.FirestoreMapper.getMoodColorSyncId
import uk.co.zlurgg.thedayto.sync.data.mapper.FirestoreMapper.resolveEntryConflict
import uk.co.zlurgg.thedayto.sync.data.mapper.FirestoreMapper.resolveMoodColorConflict
import uk.co.zlurgg.thedayto.sync.data.mapper.FirestoreMapper.toEntry
import uk.co.zlurgg.thedayto.sync.data.mapper.FirestoreMapper.toFirestoreMap
import uk.co.zlurgg.thedayto.sync.data.mapper.FirestoreMapper.toMoodColor
import uk.co.zlurgg.thedayto.sync.domain.model.SyncResult
import uk.co.zlurgg.thedayto.sync.domain.model.SyncState
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus
import uk.co.zlurgg.thedayto.sync.domain.repository.SyncRepository
import java.util.UUID

/**
 * Exception wrapper for sync errors to propagate through runCatching.
 */
private class SyncException(val error: DataError.Sync) : Exception()

/**
 * Firestore implementation of SyncRepository.
 *
 * Handles bidirectional sync between Room and Firestore.
 */
class SyncRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val entryDao: EntryDao,
    private val moodColorDao: MoodColorDao,
    private val pendingSyncDeletionDao: PendingSyncDeletionDao,
    private val notificationSyncService: NotificationSyncService
) : SyncRepository {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)

    override fun observeSyncState(): Flow<SyncState> = _syncState

    override suspend fun uploadPendingEntries(
        entries: List<Entry>,
        userId: String
    ): Result<Int, DataError.Sync> = runCatching {
        var uploadedCount = 0

        // Batch fetch all mood colors to avoid N+1 queries
        // Chunk to stay under SQLite's 999 parameter limit
        val moodColorIds = entries.map { it.moodColorId }.distinct()
        val moodColorsMap = moodColorIds
            .chunked(SQLITE_BATCH_SIZE)
            .flatMap { chunk -> moodColorDao.getMoodColorsByIds(chunk) }
            .associateBy { it.id }

        entries.forEach { entry ->
            // Get the moodColor's syncId for the reference (O(1) lookup)
            val moodColor = moodColorsMap[entry.moodColorId]
            val moodColorSyncId = moodColor?.syncId

            // Generate syncId if not present
            val syncId = entry.syncId ?: UUID.randomUUID().toString()

            val docRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ENTRIES_COLLECTION)
                .document(syncId)

            docRef.set(entry.toFirestoreMap(moodColorSyncId)).await()
            // Update local entry with sync info (only if not modified during sync)
            entry.id?.let { id ->
                entry.updatedAt?.let { uploadedAt ->
                    entryDao.updateSyncFields(
                        id = id,
                        syncId = syncId,
                        userId = userId,
                        syncStatus = SyncStatus.SYNCED.name,
                        expectedUpdatedAt = uploadedAt
                    )
                }
            }
            uploadedCount++
        }

        uploadedCount
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { mapFirestoreException(it) }
    )

    /**
     * Process pending entry deletions from the tracking table.
     * Deletes from Firestore and removes tracking record on success.
     */
    private suspend fun processPendingEntryDeletions(): Int {
        val pendingDeletions = pendingSyncDeletionDao.getByCollection(
            PendingSyncDeletionEntity.COLLECTION_ENTRIES
        )
        var deletedCount = 0

        pendingDeletions.forEach { deletion ->
            try {
                firestore
                    .collection(USERS_COLLECTION)
                    .document(deletion.userId)
                    .collection(ENTRIES_COLLECTION)
                    .document(deletion.syncId)
                    .delete()
                    .await()

                // Successfully deleted from Firestore, remove tracking record
                pendingSyncDeletionDao.delete(deletion.id)
                deletedCount++
                Timber.d("Deleted entry from Firestore: syncId=%s", deletion.syncId)
            } catch (e: Exception) {
                Timber.w(e, "Failed to delete entry from Firestore: syncId=%s", deletion.syncId)
                // Keep the tracking record so we retry next sync
            }
        }

        return deletedCount
    }

    override suspend fun uploadPendingMoodColors(
        moodColors: List<MoodColor>,
        userId: String
    ): Result<Int, DataError.Sync> = runCatching {
        var uploadedCount = 0

        moodColors.forEach { moodColor ->
            val syncId = moodColor.syncId ?: UUID.randomUUID().toString()

            val docRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOOD_COLORS_COLLECTION)
                .document(syncId)

            // Upload mood color (including soft-deletes with isDeleted=true)
            // This ensures deleted mood colors sync to other devices and can be reactivated
            docRef.set(moodColor.toFirestoreMap()).await()
            // Update local mood color with sync info (only if not modified during sync)
            moodColor.id?.let { id ->
                moodColor.updatedAt?.let { uploadedAt ->
                    moodColorDao.updateSyncFields(
                        id = id,
                        syncId = syncId,
                        userId = userId,
                        syncStatus = SyncStatus.SYNCED.name,
                        expectedUpdatedAt = uploadedAt
                    )
                }
            }
            uploadedCount++
        }

        uploadedCount
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { mapFirestoreException(it) }
    )

    override suspend fun downloadEntries(userId: String): Result<List<Entry>, DataError.Sync> =
        runCatching {
            val snapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ENTRIES_COLLECTION)
                .get()
                .await()

            Timber.d("Downloaded %d entries from Firestore", snapshot.documents.size)

            snapshot.documents.mapNotNull { doc ->
                // We need to resolve moodColorId from moodColorSyncId
                val moodColorSyncId = doc.getMoodColorSyncId()
                val moodColorEntity = moodColorSyncId?.let {
                    moodColorDao.getMoodColorBySyncId(it)
                }
                val moodColorId = moodColorEntity?.id
                if (moodColorId == null) {
                    Timber.w(
                        "Skipping entry %s: moodColorSyncId=%s not found locally",
                        doc.id,
                        moodColorSyncId
                    )
                    return@mapNotNull null
                }

                // Check if we have a local version
                val localEntry = entryDao.getEntryBySyncId(doc.id)

                doc.toEntry(moodColorId, localEntry?.id)
            }
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { mapFirestoreException(it) }
        )

    override suspend fun downloadMoodColors(userId: String): Result<List<MoodColor>, DataError.Sync> =
        runCatching {
            val snapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOOD_COLORS_COLLECTION)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val localMoodColor = moodColorDao.getMoodColorBySyncId(doc.id)
                doc.toMoodColor(localMoodColor?.id)
            }
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { mapFirestoreException(it) }
        )

    override suspend fun deleteEntry(syncId: String, userId: String): EmptyResult<DataError.Sync> =
        runCatching {
            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ENTRIES_COLLECTION)
                .document(syncId)
                .delete()
                .await()
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { mapFirestoreException(it) }
        )

    override suspend fun deleteMoodColor(
        syncId: String,
        userId: String
    ): EmptyResult<DataError.Sync> = runCatching {
        firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(MOOD_COLORS_COLLECTION)
            .document(syncId)
            .delete()
            .await()
    }.fold(
        onSuccess = { Result.Success(Unit) },
        onFailure = { mapFirestoreException(it) }
    )

    override suspend fun performFullSync(userId: String): Result<SyncResult, DataError.Sync> {
        _syncState.value = SyncState.Syncing(PROGRESS_IDLE)

        return runCatching {
            val counts = SyncCounts()

            // Phase 0: Process pending deletions FIRST (before download to avoid re-fetching)
            _syncState.value = SyncState.Syncing(PROGRESS_PHASE_0)
            Timber.d("Phase 0: Processing pending entry deletions")
            counts.entriesDeleted = processPendingEntryDeletions()
            if (counts.entriesDeleted > 0) {
                Timber.i("Deleted %d entries from Firestore", counts.entriesDeleted)
            }

            // Phase 1: Download remote mood colors first (prevents overwriting user data)
            _syncState.value = SyncState.Syncing(PROGRESS_PHASE_1)
            Timber.d("Phase 1: Downloading mood colors for user %s", userId)
            processDownloadedMoodColors(userId, counts)

            // Phase 2: Download remote entries
            _syncState.value = SyncState.Syncing(PROGRESS_PHASE_2)
            Timber.d("Phase 2: Downloading entries for user %s", userId)
            processDownloadedEntries(userId, counts)

            // Phase 3: Upload pending mood colors (after download to avoid overwriting)
            _syncState.value = SyncState.Syncing(PROGRESS_PHASE_3)
            Timber.d("Phase 3: Uploading pending mood colors")
            counts.moodColorsUploaded = uploadMoodColorsOrThrow(userId)

            // Phase 4: Upload pending entries
            _syncState.value = SyncState.Syncing(PROGRESS_PHASE_4)
            Timber.d("Phase 4: Uploading pending entries")
            counts.entriesUploaded = uploadEntriesOrThrow(userId)

            // Phase 5: Download notification settings
            _syncState.value = SyncState.Syncing(PROGRESS_PHASE_5)
            Timber.d("Phase 5: Downloading notification settings")
            when (val result = notificationSyncService.download(userId)) {
                is Result.Success -> if (result.data != null) counts.notificationSettingsDownloaded = 1
                is Result.Error -> throw SyncException(result.error)
            }

            // Phase 6: Upload pending notification settings
            _syncState.value = SyncState.Syncing(PROGRESS_PHASE_6)
            Timber.d("Phase 6: Uploading notification settings")
            counts.notificationSettingsUploaded = when (val result = notificationSyncService.uploadPending(userId)) {
                is Result.Success -> result.data
                is Result.Error -> throw SyncException(result.error)
            }

            Timber.i(
                "Sync complete: entries(up=%d, down=%d) moodColors(up=%d, down=%d) " +
                    "notificationSettings(up=%d, down=%d) conflicts=%d",
                counts.entriesUploaded,
                counts.entriesDownloaded,
                counts.moodColorsUploaded,
                counts.moodColorsDownloaded,
                counts.notificationSettingsUploaded,
                counts.notificationSettingsDownloaded,
                counts.conflictsResolved
            )
            counts.toSyncResult()
        }.fold(
            onSuccess = {
                _syncState.value = SyncState.Success(it)
                Result.Success(it)
            },
            onFailure = {
                val error = mapFirestoreException<SyncResult>(it)
                _syncState.value = SyncState.Error((error as Result.Error).error)
                error
            }
        )
    }

    private suspend fun uploadMoodColorsOrThrow(userId: String): Int {
        val pendingMoodColors = moodColorDao.getMoodColorsPendingSync().map { it.toDomain() }
        return when (val result = uploadPendingMoodColors(pendingMoodColors, userId)) {
            is Result.Success -> result.data
            is Result.Error -> throw SyncException(result.error)
        }
    }

    private suspend fun uploadEntriesOrThrow(userId: String): Int {
        val pendingEntries = entryDao.getEntriesPendingSync().map { it.toDomain() }
        return when (val result = uploadPendingEntries(pendingEntries, userId)) {
            is Result.Success -> result.data
            is Result.Error -> throw SyncException(result.error)
        }
    }

    override suspend fun clearRemoteData(userId: String): EmptyResult<DataError.Sync> = runCatching {
        // Delete all entries
        val entriesSnapshot = firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(ENTRIES_COLLECTION)
            .get()
            .await()

        entriesSnapshot.documents.forEach { doc ->
            doc.reference.delete().await()
        }

        // Delete all mood colors
        val moodColorsSnapshot = firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(MOOD_COLORS_COLLECTION)
            .get()
            .await()

        moodColorsSnapshot.documents.forEach { doc ->
            doc.reference.delete().await()
        }

        // Delete notification settings
        val notificationSettingsSnapshot = firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(NOTIFICATION_SETTINGS_COLLECTION)
            .get()
            .await()

        notificationSettingsSnapshot.documents.forEach { doc ->
            doc.reference.delete().await()
        }
    }.fold(
        onSuccess = { Result.Success(Unit) },
        onFailure = { mapFirestoreException(it) }
    )

    override suspend fun markLocalDataForSync(): Int {
        val moodColorsMarked = moodColorDao.markLocalOnlyAsPendingSync()
        val entriesMarked = entryDao.markLocalOnlyAsPendingSync()
        val total = moodColorsMarked + entriesMarked
        Timber.i("Marked %d items for sync (moodColors=%d, entries=%d)", total, moodColorsMarked, entriesMarked)
        return total
    }

    override suspend fun adoptOrphanedData(userId: String): Int {
        val moodColorsAdopted = moodColorDao.adoptOrphans(userId)
        val entriesAdopted = entryDao.adoptOrphans(userId)
        val total = moodColorsAdopted + entriesAdopted
        Timber.i(
            "Adopted %d orphaned items (moodColors=%d, entries=%d)",
            total,
            moodColorsAdopted,
            entriesAdopted
        )
        return total
    }

    override suspend fun markSyncedAsLocalOnly(): Int {
        val moodColorsReset = moodColorDao.markSyncedAsLocalOnly()
        val entriesReset = entryDao.markSyncedAsLocalOnly()
        val total = moodColorsReset + entriesReset
        Timber.i(
            "Reset %d synced items to LOCAL_ONLY (moodColors=%d, entries=%d)",
            total,
            moodColorsReset,
            entriesReset
        )
        return total
    }

    override suspend fun clearOtherUserData(currentUserId: String): Int {
        // Find all userIds that are not the current user
        val moodColorUserIds = moodColorDao.getDistinctUserIds()
        val entryUserIds = entryDao.getDistinctUserIds()
        val allUserIds = (moodColorUserIds + entryUserIds).distinct()
        val otherUserIds = allUserIds.filter { it != currentUserId }

        if (otherUserIds.isEmpty()) {
            Timber.d("No other user data to clear")
            return 0
        }

        var totalDeleted = 0
        otherUserIds.forEach { userId ->
            val entriesDeleted = entryDao.deleteByUserId(userId)
            val moodColorsDeleted = moodColorDao.deleteByUserId(userId)
            totalDeleted += entriesDeleted + moodColorsDeleted
            Timber.i(
                "Cleared data for user %s: entries=%d, moodColors=%d",
                userId,
                entriesDeleted,
                moodColorsDeleted
            )
        }

        return totalDeleted
    }

    override suspend fun clearUserData(userId: String): Int {
        val entriesDeleted = entryDao.deleteByUserId(userId)
        val moodColorsDeleted = moodColorDao.deleteByUserId(userId)
        val total = entriesDeleted + moodColorsDeleted
        Timber.i(
            "Cleared user data on sign-out: entries=%d, moodColors=%d",
            entriesDeleted,
            moodColorsDeleted
        )
        return total
    }

    /**
     * Process downloaded mood colors and merge with local data.
     * @throws SyncException if download fails
     */
    private suspend fun processDownloadedMoodColors(userId: String, counts: SyncCounts) {
        val remoteMoodColors = when (val result = downloadMoodColors(userId)) {
            is Result.Success -> result.data
            is Result.Error -> throw SyncException(result.error)
        }

        Timber.d("Downloaded %d mood colors from Firestore", remoteMoodColors.size)
        remoteMoodColors.forEach { remote ->
            mergeMoodColor(remote, counts)
        }
    }

    private suspend fun mergeMoodColor(remote: MoodColor, counts: SyncCounts) {
        val local = remote.syncId?.let { moodColorDao.getMoodColorBySyncId(it)?.toDomain() }

        if (local == null) {
            Timber.d("Inserting new mood color: %s (syncId=%s)", remote.mood, remote.syncId)
            moodColorDao.insertMoodColor(remote.toEntity())
            counts.moodColorsDownloaded++
            return
        }

        val resolved = resolveMoodColorConflict(local, remote)
        if (resolved == local) return

        moodColorDao.updateMoodColor(resolved.toEntity())
        counts.moodColorsDownloaded++
        if (resolved.updatedAt != remote.updatedAt) {
            counts.conflictsResolved++
        }
    }

    /**
     * Process downloaded entries and merge with local data.
     * @throws SyncException if download fails
     */
    private suspend fun processDownloadedEntries(userId: String, counts: SyncCounts) {
        val remoteEntries = when (val result = downloadEntries(userId)) {
            is Result.Success -> result.data
            is Result.Error -> throw SyncException(result.error)
        }

        Timber.d("Downloaded %d entries from Firestore", remoteEntries.size)
        remoteEntries.forEach { remote ->
            mergeEntry(remote, counts)
        }
    }

    private suspend fun mergeEntry(remote: Entry, counts: SyncCounts) {
        val local = remote.syncId?.let { entryDao.getEntryBySyncId(it)?.toDomain() }

        if (local == null) {
            entryDao.insertEntry(remote.toEntity())
            counts.entriesDownloaded++
            return
        }

        val resolved = resolveEntryConflict(local, remote)
        if (resolved == local) return

        entryDao.updateEntry(resolved.toEntity())
        counts.entriesDownloaded++
        if (resolved.updatedAt != remote.updatedAt) {
            counts.conflictsResolved++
        }
    }

    private fun <T> mapFirestoreException(throwable: Throwable): Result<T, DataError.Sync> {
        // SyncException wraps already-mapped errors, just unwrap
        if (throwable is SyncException) {
            return Result.Error(throwable.error)
        }

        Timber.e(throwable, "Firestore sync error")

        val error = when (throwable) {
            is FirebaseFirestoreException -> mapFirebaseException(throwable)
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.net.ConnectException -> {
                logEmulatorHint()
                DataError.Sync.NETWORK_ERROR
            }
            else -> DataError.Sync.UNKNOWN
        }

        return Result.Error(error)
    }

    private fun mapFirebaseException(e: FirebaseFirestoreException): DataError.Sync = when (e.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> DataError.Sync.PERMISSION_DENIED
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> DataError.Sync.NOT_AUTHENTICATED
        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> DataError.Sync.QUOTA_EXCEEDED
        FirebaseFirestoreException.Code.UNAVAILABLE -> {
            logEmulatorHint()
            DataError.Sync.NETWORK_ERROR
        }
        else -> DataError.Sync.UNKNOWN
    }

    private fun logEmulatorHint() {
        Timber.w(
            "Firestore connection failed. If running locally, ensure Firebase emulator is started:\n" +
                "  Run: scripts\\start-emulator.bat"
        )
    }

    /**
     * Mutable container for tracking sync operation counts.
     */
    private class SyncCounts {
        var entriesUploaded: Int = 0
        var entriesDownloaded: Int = 0
        var entriesDeleted: Int = 0
        var moodColorsUploaded: Int = 0
        var moodColorsDownloaded: Int = 0
        var notificationSettingsUploaded: Int = 0
        var notificationSettingsDownloaded: Int = 0
        var conflictsResolved: Int = 0

        fun toSyncResult() = SyncResult(
            entriesUploaded = entriesUploaded,
            entriesDownloaded = entriesDownloaded,
            moodColorsUploaded = moodColorsUploaded,
            moodColorsDownloaded = moodColorsDownloaded,
            notificationSettingsUploaded = notificationSettingsUploaded,
            notificationSettingsDownloaded = notificationSettingsDownloaded,
            conflictsResolved = conflictsResolved
        )
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val ENTRIES_COLLECTION = "entries"
        private const val MOOD_COLORS_COLLECTION = "mood_colors"
        private const val NOTIFICATION_SETTINGS_COLLECTION = "notification_settings"

        // SQLite has a ~999 parameter limit for IN clauses
        private const val SQLITE_BATCH_SIZE = 500

        // Progress constants for sync phases
        private const val PROGRESS_IDLE = 0f
        private const val PROGRESS_PHASE_0 = 0.05f
        private const val PROGRESS_PHASE_1 = 0.1f
        private const val PROGRESS_PHASE_2 = 0.25f
        private const val PROGRESS_PHASE_3 = 0.45f
        private const val PROGRESS_PHASE_4 = 0.65f
        private const val PROGRESS_PHASE_5 = 0.8f
        private const val PROGRESS_PHASE_6 = 0.9f
    }
}
