package uk.co.zlurgg.thedayto.journal.domain.model

import androidx.compose.runtime.Immutable
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus

@Immutable
data class MoodColor(
    val mood: String,
    val color: String,
    val isDeleted: Boolean = false,
    val dateStamp: Long,
    val id: Int? = null,
    val syncId: String? = null,
    val userId: String? = null,
    val updatedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
)

class InvalidMoodColorException(message: String) : Exception(message)