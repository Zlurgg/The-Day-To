package uk.co.zlurgg.thedayto.journal.domain.model

import androidx.compose.runtime.Immutable
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus

@Immutable
data class MoodColor(
    val mood: String,
    val color: String,
    val isDeleted: Boolean = false,
    val isFavorite: Boolean = false,
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