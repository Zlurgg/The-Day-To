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
import uk.co.zlurgg.thedayto.journal.data.mapper.toDomain
import uk.co.zlurgg.thedayto.journal.data.mapper.toEntity
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
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
 * Firestore implementation of SyncRepository.
 *
 * Handles bidirectional sync between Room and Firestore.
 */
@Suppress("MagicNumber", "ReturnCount")
class SyncRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val entryDao: EntryDao,
    private val moodColorDao: MoodColorDao
) : SyncRepository {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)

    override fun observeSyncState(): Flow<SyncState> = _syncState

    override suspend fun uploadPendingEntries(
        entries: List<Entry>,
        userId: String
    ): Result<Int, DataError.Sync> = runCatching {
        var uploadedCount = 0

        entries.forEach { entry ->
            // Get the moodColor's syncId for the reference
            val moodColor = moodColorDao.getMoodColorById(entry.moodColorId)
            val moodColorSyncId = moodColor?.syncId

            // Generate syncId if not present
            val syncId = entry.syncId ?: UUID.randomUUID().toString()

            val docRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ENTRIES_COLLECTION)
                .document(syncId)

            if (entry.syncStatus == SyncStatus.PENDING_DELETE) {
                docRef.delete().await()
                // Hard delete locally after successful remote delete
                entry.id?.let { id ->
                    val entityToDelete = entryDao.getEntryById(id)
                    entityToDelete?.let { entryDao.deleteEntry(it) }
                }
            } else {
                docRef.set(entry.toFirestoreMap(moodColorSyncId)).await()
                // Update local entry with sync info
                entry.id?.let { id ->
                    entryDao.updateSyncFields(
                        id = id,
                        syncId = syncId,
                        userId = userId,
                        updatedAt = System.currentTimeMillis(),
                        syncStatus = SyncStatus.SYNCED.name
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

            if (moodColor.syncStatus == SyncStatus.PENDING_DELETE) {
                docRef.delete().await()
                // For mood colors, we keep the soft-delete locally
                moodColor.id?.let { id ->
                    moodColorDao.updateSyncStatus(id, SyncStatus.SYNCED.name)
                }
            } else {
                docRef.set(moodColor.toFirestoreMap()).await()
                moodColor.id?.let { id ->
                    moodColorDao.updateSyncFields(
                        id = id,
                        syncId = syncId,
                        userId = userId,
                        updatedAt = System.currentTimeMillis(),
                        syncStatus = SyncStatus.SYNCED.name
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

            snapshot.documents.mapNotNull { doc ->
                // We need to resolve moodColorId from moodColorSyncId
                val moodColorSyncId = doc.getMoodColorSyncId()
                val moodColorEntity = moodColorSyncId?.let {
                    moodColorDao.getMoodColorBySyncId(it)
                }
                val moodColorId = moodColorEntity?.id ?: return@mapNotNull null

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
        _syncState.value = SyncState.Syncing(0f)

        return runCatching {
            var entriesUploaded = 0
            var entriesDownloaded = 0
            var moodColorsUploaded = 0
            var moodColorsDownloaded = 0
            var conflictsResolved = 0

            // Phase 1: Upload pending mood colors first (entries depend on them)
            _syncState.value = SyncState.Syncing(0.1f)
            val pendingMoodColors = moodColorDao.getMoodColorsPendingSync().map { it.toDomain() }
            when (val result = uploadPendingMoodColors(pendingMoodColors, userId)) {
                is Result.Success -> moodColorsUploaded = result.data
                is Result.Error -> return result
            }

            // Phase 2: Upload pending entries
            _syncState.value = SyncState.Syncing(0.3f)
            val pendingEntries = entryDao.getEntriesPendingSync().map { it.toDomain() }
            when (val result = uploadPendingEntries(pendingEntries, userId)) {
                is Result.Success -> entriesUploaded = result.data
                is Result.Error -> return result
            }

            // Phase 3: Download remote mood colors
            _syncState.value = SyncState.Syncing(0.5f)
            when (val result = downloadMoodColors(userId)) {
                is Result.Success -> {
                    result.data.forEach { remoteMoodColor ->
                        val localMoodColor = remoteMoodColor.syncId?.let {
                            moodColorDao.getMoodColorBySyncId(it)?.toDomain()
                        }

                        if (localMoodColor == null) {
                            // New remote mood color - insert locally
                            moodColorDao.insertMoodColor(remoteMoodColor.toEntity())
                            moodColorsDownloaded++
                        } else {
                            // Conflict resolution
                            val resolved = resolveMoodColorConflict(localMoodColor, remoteMoodColor)
                            if (resolved != localMoodColor) {
                                moodColorDao.updateMoodColor(resolved.toEntity())
                                moodColorsDownloaded++
                                if (resolved.updatedAt != remoteMoodColor.updatedAt) {
                                    conflictsResolved++
                                }
                            }
                        }
                    }
                }
                is Result.Error -> return result
            }

            // Phase 4: Download remote entries
            _syncState.value = SyncState.Syncing(0.8f)
            when (val result = downloadEntries(userId)) {
                is Result.Success -> {
                    result.data.forEach { remoteEntry ->
                        val localEntry = remoteEntry.syncId?.let {
                            entryDao.getEntryBySyncId(it)?.toDomain()
                        }

                        if (localEntry == null) {
                            // New remote entry - insert locally
                            entryDao.insertEntry(remoteEntry.toEntity())
                            entriesDownloaded++
                        } else {
                            // Conflict resolution
                            val resolved = resolveEntryConflict(localEntry, remoteEntry)
                            if (resolved != localEntry) {
                                entryDao.updateEntry(resolved.toEntity())
                                entriesDownloaded++
                                if (resolved.updatedAt != remoteEntry.updatedAt) {
                                    conflictsResolved++
                                }
                            }
                        }
                    }
                }
                is Result.Error -> return result
            }

            SyncResult(
                entriesUploaded = entriesUploaded,
                entriesDownloaded = entriesDownloaded,
                moodColorsUploaded = moodColorsUploaded,
                moodColorsDownloaded = moodColorsDownloaded,
                conflictsResolved = conflictsResolved
            )
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
    }.fold(
        onSuccess = { Result.Success(Unit) },
        onFailure = { mapFirestoreException(it) }
    )

    private fun <T> mapFirestoreException(throwable: Throwable): Result<T, DataError.Sync> {
        Timber.e(throwable, "Firestore sync error")

        val error = when (throwable) {
            is FirebaseFirestoreException -> when (throwable.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> DataError.Sync.PERMISSION_DENIED
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> DataError.Sync.NOT_AUTHENTICATED
                FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> DataError.Sync.QUOTA_EXCEEDED
                FirebaseFirestoreException.Code.UNAVAILABLE -> {
                    logEmulatorHint()
                    DataError.Sync.NETWORK_ERROR
                }
                else -> DataError.Sync.UNKNOWN
            }
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

    private fun logEmulatorHint() {
        Timber.w(
            "Firestore connection failed. If running locally, ensure Firebase emulator is started:\n" +
                "  Run: scripts\\start-emulator.bat"
        )
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val ENTRIES_COLLECTION = "entries"
        private const val MOOD_COLORS_COLLECTION = "mood_colors"
    }
}
