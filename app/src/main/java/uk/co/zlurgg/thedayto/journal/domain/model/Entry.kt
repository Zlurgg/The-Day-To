package uk.co.zlurgg.thedayto.journal.domain.model

import androidx.compose.runtime.Immutable
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus

@Immutable
data class Entry(
    val moodColorId: Int,
    val content: String,
    /**
     * The date this entry is for, stored as **epoch seconds at start-of-day UTC**.
     * Populated via `LocalDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond()`.
     *
     * Note: this is a *date*, not a creation timestamp. It uses the same coarse
     * granularity as the calendar grid so the day cell can look up entries by
     * exact equality. Do not confuse with [MoodColor.dateStamp], which uses
     * milliseconds and represents a creation timestamp.
     */
    val dateStamp: Long,
    val id: Int? = null,
    val syncId: String? = null,
    val userId: String? = null,
    val updatedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
)

class InvalidEntryException(message: String) : Exception(message)
