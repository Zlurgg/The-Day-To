package uk.co.zlurgg.thedayto.sync.domain.model

import uk.co.zlurgg.thedayto.core.domain.error.DataError

/**
 * Represents the current state of cloud sync.
 */
sealed interface SyncState {
    /** Sync is not active */
    data object Idle : SyncState

    /** Sync is currently in progress */
    data class Syncing(val progress: Float = 0f) : SyncState

    /** Sync completed successfully */
    data class Success(val result: SyncResult) : SyncState

    /** Sync failed with an error */
    data class Error(val error: DataError.Sync) : SyncState
}
