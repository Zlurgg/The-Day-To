package uk.co.zlurgg.thedayto.sync.ui

import uk.co.zlurgg.thedayto.sync.domain.model.SyncState

/**
 * UI state for the Sync Settings screen.
 */
data class SyncSettingsState(
    val isSyncEnabled: Boolean = false,
    val isUserSignedIn: Boolean = false,
    val userEmail: String? = null,
    val isSigningIn: Boolean = false,
    val isDevSignInAvailable: Boolean = false,
    val syncState: SyncState = SyncState.Idle,
    val lastSyncTimestamp: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
