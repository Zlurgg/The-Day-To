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
        /** Fallback color for a brand-new mood color when the user has no prior selection. */
        const val DEFAULT_EMPTY_COLOR = "CCCCCC"

        /**
         * Creates an empty mood color for the add dialog.
         * Uses a unique timestamp so LaunchedEffect re-runs when the dialog
         * reopens, and accepts an optional seed color so callers can
         * remember the user's last-picked color within a session.
         */
        fun empty(color: String = DEFAULT_EMPTY_COLOR) = MoodColor(
            mood = "",
            color = color,
            dateStamp = System.currentTimeMillis()
        )
    }
}