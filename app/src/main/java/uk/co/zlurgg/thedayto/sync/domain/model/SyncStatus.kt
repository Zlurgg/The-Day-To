package uk.co.zlurgg.thedayto.sync.domain.model

import timber.log.Timber

/**
 * Represents the synchronization status of a local entity.
 */
enum class SyncStatus {
    /** Never synced to cloud - local only data */
    LOCAL_ONLY,

    /** Changed locally, needs to be uploaded to Firestore */
    PENDING_SYNC,

    /** In sync with Firestore */
    SYNCED,

    /** Marked for deletion, needs to sync delete to Firestore */
    PENDING_DELETE
}

/**
 * Safely converts a String to SyncStatus, defaulting to LOCAL_ONLY on invalid values.
 * Prevents crashes from corrupted database values or future enum changes.
 */
fun String.toSyncStatusOrDefault(): SyncStatus = try {
    SyncStatus.valueOf(this)
} catch (e: IllegalArgumentException) {
    Timber.w(e, "Unknown sync status: %s, defaulting to LOCAL_ONLY", this)
    SyncStatus.LOCAL_ONLY
}
