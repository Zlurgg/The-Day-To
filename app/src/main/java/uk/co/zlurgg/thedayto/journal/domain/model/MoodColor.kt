package uk.co.zlurgg.thedayto.journal.domain.model

import androidx.compose.runtime.Immutable
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus

@Immutable
data class MoodColor(
    val mood: String,
    val color: String,
    val isDeleted: Boolean = false,
    val isFavorite: Boolean = false,
    /**
     * Creation timestamp, stored as **epoch milliseconds**
     * (`System.currentTimeMillis()`).
     *
     * Used for sort-by-creation in the management screen and for unique key
     * generation in dialog state. Note: this is a wall-clock timestamp, not
     * a date. Do not confuse with [Entry.dateStamp], which uses epoch
     * *seconds* at start-of-day UTC and represents a date.
     */
    val dateStamp: Long,
    val id: Int? = null,
    val syncId: String? = null,
    val userId: String? = null,
    val updatedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) {
    companion object {
        /**
         * Creates an empty mood color for the add dialog.
         * Uses unique timestamp to ensure LaunchedEffect re-runs.
         */
        fun empty() = MoodColor(
            mood = "",
            color = "CCCCCC",
            dateStamp = System.currentTimeMillis()
        )
    }
}