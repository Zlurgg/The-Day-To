package uk.co.zlurgg.thedayto.sync.data.mapper

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus

/**
 * Mapper for converting between domain models and Firestore documents.
 */
object FirestoreMapper {

    private const val MILLIS_PER_SECOND = 1000L
    private const val ZERO_NANOSECONDS = 0

    // ==================== Entry Mapping ====================

    /**
     * Convert Entry domain model to Firestore document map.
     * Note: Entries are hard-deleted, so isDeleted is not stored in Firestore.
     */
    fun Entry.toFirestoreMap(moodColorSyncId: String?): Map<String, Any?> = mapOf(
        "moodColorSyncId" to moodColorSyncId,
        "content" to content,
        "dateStamp" to dateStamp,
        "updatedAt" to (updatedAt?.let { Timestamp(it / MILLIS_PER_SECOND, ZERO_NANOSECONDS) } ?: Timestamp.now())
    )

    /**
     * Convert Firestore document to Entry domain model.
     * Note: Entries are hard-deleted, so isDeleted is not read from Firestore.
     */
    fun DocumentSnapshot.toEntry(
        moodColorId: Int,
        localId: Int? = null
    ): Entry? {
        if (!exists()) return null

        return Entry(
            moodColorId = moodColorId,
            content = getString("content") ?: "",
            dateStamp = getLong("dateStamp") ?: 0L,
            id = localId,
            syncId = id,
            userId = reference.parent.parent?.id,
            updatedAt = getTimestamp("updatedAt")?.seconds?.times(MILLIS_PER_SECOND),
            syncStatus = SyncStatus.SYNCED
        )
    }

    /**
     * Extract moodColorSyncId from an entry Firestore document.
     */
    fun DocumentSnapshot.getMoodColorSyncId(): String? = getString("moodColorSyncId")

    // ==================== MoodColor Mapping ====================

    /**
     * Convert MoodColor domain model to Firestore document map.
     * Seeds (updatedAt = 0) get current timestamp so they sync properly.
     */
    fun MoodColor.toFirestoreMap(): Map<String, Any?> = mapOf(
        "mood" to mood,
        "color" to color,
        "isDeleted" to isDeleted,
        "dateStamp" to dateStamp,
        "updatedAt" to when {
            updatedAt == null || updatedAt == 0L -> Timestamp.now()
            else -> Timestamp(updatedAt / MILLIS_PER_SECOND, ZERO_NANOSECONDS)
        }
    )

    /**
     * Convert Firestore document to MoodColor domain model.
     */
    fun DocumentSnapshot.toMoodColor(localId: Int? = null): MoodColor? {
        if (!exists()) return null

        return MoodColor(
            mood = getString("mood") ?: "",
            color = getString("color") ?: "",
            isDeleted = getBoolean("isDeleted") ?: false,
            dateStamp = getLong("dateStamp") ?: 0L,
            id = localId,
            syncId = id,
            userId = reference.parent.parent?.id,
            updatedAt = getTimestamp("updatedAt")?.seconds?.times(MILLIS_PER_SECOND),
            syncStatus = SyncStatus.SYNCED
        )
    }

    // ==================== Conflict Resolution ====================

    /**
     * Resolve conflict between local and remote entries using last-write-wins.
     * Never overwrites pending local changes - let upload phase handle the conflict.
     * Exception: Seeds (updatedAt <= 0) are always overwritten by remote.
     *
     * Note: Entries are hard-deleted, so PENDING_DELETE is not applicable here.
     * Deletions are tracked separately in pending_sync_deletion table.
     *
     * @param local Local entry
     * @param remote Remote entry
     * @return The winning entry (local if pending, remote if newer, or local marked for re-upload)
     */
    fun resolveEntryConflict(local: Entry, remote: Entry): Entry {
        val localTime = local.updatedAt ?: 0L
        val remoteTime = remote.updatedAt ?: 0L

        // Seeds (updatedAt <= 0) are always overwritten by remote data
        val isLocalSeed = localTime <= 0L

        // Never overwrite pending local changes, unless it's a seed
        if (local.syncStatus == SyncStatus.PENDING_SYNC && !isLocalSeed) {
            return local
        }

        return if (remoteTime >= localTime) {
            // Remote wins - preserve local Room ID
            remote.copy(id = local.id)
        } else {
            // Local wins - mark for re-upload
            local.copy(syncStatus = SyncStatus.PENDING_SYNC)
        }
    }

    /**
     * Resolve conflict between local and remote mood colors using last-write-wins.
     * Never overwrites pending local changes - let upload phase handle the conflict.
     * Exception: Seeds (updatedAt <= 0) are always overwritten by remote.
     */
    fun resolveMoodColorConflict(local: MoodColor, remote: MoodColor): MoodColor {
        val localTime = local.updatedAt ?: 0L
        val remoteTime = remote.updatedAt ?: 0L

        // Seeds (updatedAt <= 0) are always overwritten by remote data
        val isLocalSeed = localTime <= 0L

        // Never overwrite pending local changes (PENDING_SYNC or PENDING_DELETE), unless it's a seed
        val hasPendingChanges = local.syncStatus == SyncStatus.PENDING_SYNC ||
            local.syncStatus == SyncStatus.PENDING_DELETE
        if (hasPendingChanges && !isLocalSeed) {
            return local
        }

        return if (remoteTime >= localTime) {
            // Remote wins - preserve local Room ID
            remote.copy(id = local.id)
        } else {
            // Local wins - mark for re-upload
            local.copy(syncStatus = SyncStatus.PENDING_SYNC)
        }
    }
}
