package uk.co.zlurgg.thedayto.sync.ui

import uk.co.zlurgg.thedayto.auth.domain.usecases.DeletionProgress
import uk.co.zlurgg.thedayto.sync.domain.model.SyncState

/**
 * UI state for the Account screen.
 *
 * Sync is automatically enabled when signed in - no separate toggle needed.
 */
data class SyncSettingsState(
    val isUserSignedIn: Boolean = false,
    val userEmail: String? = null,
    val isSigningIn: Boolean = false,
    val isDevSignInAvailable: Boolean = false,
    val syncState: SyncState = SyncState.Idle,
    val lastSyncTimestamp: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val deletionProgress: DeletionProgress? = null,
    val showReAuthDialog: Boolean = false
)
