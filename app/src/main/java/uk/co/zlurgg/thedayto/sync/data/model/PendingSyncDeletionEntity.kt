package uk.co.zlurgg.thedayto.sync.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks items that have been hard-deleted locally and need to be deleted from Firestore.
 *
 * Flow:
 * 1. User deletes an entry that was synced (has syncId)
 * 2. Record the syncId here for later Firestore deletion
 * 3. Hard delete the entry from the entry table immediately
 * 4. Sync worker processes this table and deletes from Firestore
 * 5. After successful Firestore deletion, remove from this table
 *
 * This allows immediate local deletion while still syncing deletions to Firestore.
 */
@Entity(
    tableName = "pending_sync_deletion",
    indices = [
        Index(value = ["collection", "syncId"], unique = true)
    ]
)
data class PendingSyncDeletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val syncId: String,
    val collection: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_ENTRIES = "entries"
    }
}
