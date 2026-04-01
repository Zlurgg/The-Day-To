package uk.co.zlurgg.thedayto.sync.domain.repository

import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.sync.domain.model.SyncResult
import uk.co.zlurgg.thedayto.sync.domain.model.SyncState

/**
 * Repository for cloud sync operations with Firestore.
 */
interface SyncRepository {

    /** Observe the current sync state */
    fun observeSyncState(): Flow<SyncState>

    /** Upload all pending local entries to Firestore */
    suspend fun uploadPendingEntries(
        entries: List<Entry>,
        userId: String
    ): Result<Int, DataError.Sync>

    /** Upload all pending local mood colors to Firestore */
    suspend fun uploadPendingMoodColors(
        moodColors: List<MoodColor>,
        userId: String
    ): Result<Int, DataError.Sync>

    /** Download all entries from Firestore for the given user */
    suspend fun downloadEntries(userId: String): Result<List<Entry>, DataError.Sync>

    /** Download all mood colors from Firestore for the given user */
    suspend fun downloadMoodColors(userId: String): Result<List<MoodColor>, DataError.Sync>

    /** Delete an entry from Firestore */
    suspend fun deleteEntry(syncId: String, userId: String): EmptyResult<DataError.Sync>

    /** Delete a mood color from Firestore */
    suspend fun deleteMoodColor(syncId: String, userId: String): EmptyResult<DataError.Sync>

    /** Perform a full bidirectional sync */
    suspend fun performFullSync(userId: String): Result<SyncResult, DataError.Sync>

    /** Clear all remote data for the user (used when disabling sync) */
    suspend fun clearRemoteData(userId: String): EmptyResult<DataError.Sync>
}
