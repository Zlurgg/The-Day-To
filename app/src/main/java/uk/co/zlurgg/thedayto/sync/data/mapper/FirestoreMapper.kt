package uk.co.zlurgg.thedayto.sync.data.mapper

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus

/**
 * Mapper for converting between domain models and Firestore documents.
 */
@Suppress("MagicNumber")
object FirestoreMapper {

    private const val MILLIS_PER_SECOND = 1000L

    // ==================== Entry Mapping ====================

    /**
     * Convert Entry domain model to Firestore document map.
     */
    fun Entry.toFirestoreMap(moodColorSyncId: String?): Map<String, Any?> = mapOf(
        "moodColorSyncId" to moodColorSyncId,
        "content" to content,
        "dateStamp" to dateStamp,
        "updatedAt" to Timestamp.now(),
        "createdAt" to (updatedAt?.let { Timestamp(it / MILLIS_PER_SECOND, 0) } ?: Timestamp.now())
    )

    /**
     * Convert Firestore document to Entry domain model.
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
     */
    fun MoodColor.toFirestoreMap(): Map<String, Any?> = mapOf(
        "mood" to mood,
        "color" to color,
        "isDeleted" to isDeleted,
        "dateStamp" to dateStamp,
        "updatedAt" to Timestamp.now()
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
     *
     * @param local Local entry
     * @param remote Remote entry
     * @return The winning entry (remote with local ID preserved, or local if newer)
     */
    fun resolveEntryConflict(local: Entry, remote: Entry): Entry {
        val localTime = local.updatedAt ?: 0L
        val remoteTime = remote.updatedAt ?: 0L

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
     */
    fun resolveMoodColorConflict(local: MoodColor, remote: MoodColor): MoodColor {
        val localTime = local.updatedAt ?: 0L
        val remoteTime = remote.updatedAt ?: 0L

        return if (remoteTime >= localTime) {
            // Remote wins - preserve local Room ID
            remote.copy(id = local.id)
        } else {
            // Local wins - mark for re-upload
            local.copy(syncStatus = SyncStatus.PENDING_SYNC)
        }
    }
}
