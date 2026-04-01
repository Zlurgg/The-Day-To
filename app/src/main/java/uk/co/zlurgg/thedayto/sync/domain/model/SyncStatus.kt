package uk.co.zlurgg.thedayto.sync.domain.model

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
