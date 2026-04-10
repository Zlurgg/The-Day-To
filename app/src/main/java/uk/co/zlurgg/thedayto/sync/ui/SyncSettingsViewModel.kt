package uk.co.zlurgg.thedayto.sync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.auth.domain.model.CredentialProvider
import uk.co.zlurgg.thedayto.auth.domain.usecases.AccountUseCases
import uk.co.zlurgg.thedayto.auth.domain.usecases.DeletionProgress
import uk.co.zlurgg.thedayto.core.domain.error.ErrorFormatter
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.notification.domain.usecase.NotificationAuthUseCase
import uk.co.zlurgg.thedayto.sync.DevCredentials
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler
import uk.co.zlurgg.thedayto.sync.domain.model.SyncState
import uk.co.zlurgg.thedayto.sync.domain.usecase.SyncUseCases

/**
 * ViewModel for the Account screen.
 *
 * Manages sign-in/sign-out state and triggers sync operations.
 * Sync is automatically enabled when signed in.
 */
class SyncSettingsViewModel(
    private val syncUseCases: SyncUseCases,
    private val accountUseCases: AccountUseCases,
    private val syncScheduler: SyncScheduler,
    private val notificationAuthUseCase: NotificationAuthUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncSettingsState())
    val uiState: StateFlow<SyncSettingsState> = _uiState.asStateFlow()

    init {
        loadInitialState()
        observeSyncState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            try {
                val user = accountUseCases.getSignedInUser()
                val isSignedIn = user != null
                val lastSync = syncUseCases.getLastSyncTimestamp()

                _uiState.update {
                    it.copy(
                        isUserSignedIn = isSignedIn,
                        userEmail = user?.username,
                        lastSyncTimestamp = lastSync,
                        isDevSignInAvailable = accountUseCases.devSignIn?.isAvailable() == true,
                        isLoading = false,
                    )
                }

                Timber.d("Account state loaded: signedIn=$isSignedIn")
            } catch (e: Exception) {
                Timber.e(e, "Error loading account state")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load account state",
                    )
                }
            }
        }
    }

    private fun observeSyncState() {
        viewModelScope.launch {
            syncUseCases.observeSyncState().collect { state ->
                _uiState.update { it.copy(syncState = state) }

                // Update last sync timestamp on success
                if (state is SyncState.Success) {
                    val lastSync = syncUseCases.getLastSyncTimestamp()
                    _uiState.update { it.copy(lastSyncTimestamp = lastSync) }
                }
            }
        }
    }

    fun onSyncNowClicked() {
        viewModelScope.launch {
            Timber.d("Manual sync requested")
            syncScheduler.requestImmediateSync()
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Sign in with Google credentials.
     * Called when user taps Sign In button in Account settings.
     * Automatically enables sync on successful sign-in.
     */
    fun signIn(credentialProvider: CredentialProvider) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true, error = null) }

            when (val result = accountUseCases.signIn(credentialProvider)) {
                is Result.Success -> {
                    handleSignInSuccess()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSigningIn = false,
                            error = ErrorFormatter.format(result.error, "sign in"),
                        )
                    }
                    Timber.w("Sign in failed: ${result.error}")
                }
            }
        }
    }

    /**
     * Dev sign-in with Firebase Emulator (debug builds only).
     * Called when user taps Dev Sign-In button in Account settings.
     * Automatically enables sync on successful sign-in.
     */
    fun devSignIn() {
        val useCase = accountUseCases.devSignIn ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true, error = null) }

            when (val result = useCase(DevCredentials.DEV_EMAIL, DevCredentials.DEV_PASSWORD)) {
                is Result.Success -> {
                    handleSignInSuccess()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSigningIn = false,
                            error = ErrorFormatter.format(result.error, "dev sign in"),
                        )
                    }
                    Timber.w("Dev sign in failed: ${result.error}")
                }
            }
        }
    }

    /**
     * Common success handler for sign-in operations.
     * Auto-enables sync and triggers immediate sync.
     * Migrates anonymous notification settings to the signed-in user.
     */
    private suspend fun handleSignInSuccess() {
        val user = accountUseCases.getSignedInUser() ?: return
        val userId = user.userId

        // Auto-enable sync on sign-in
        syncUseCases.setSyncEnabled(true)

        // Prepare local data for sync (clear other users, adopt orphaned, mark for upload)
        syncUseCases.prepareForSync(userId)

        // Migrate anonymous notification settings to signed-in user
        notificationAuthUseCase.handleSignInSuccess(userId)

        // Start background sync and trigger immediate first sync
        syncScheduler.startPeriodicSync()
        syncScheduler.requestImmediateSync()

        val lastSync = syncUseCases.getLastSyncTimestamp()
        _uiState.update {
            it.copy(
                isUserSignedIn = true,
                isSigningIn = false,
                userEmail = user.username,
                lastSyncTimestamp = lastSync,
                shouldNavigateBack = true,
            )
        }
        Timber.i("Sign in successful: %s", user.username)
    }

    /**
     * Sign out and disable sync.
     * Called when user taps Sign Out button in Account settings.
     * Keeps local data intact - sync is for backup/transfer, not data isolation.
     * Reverse-adopts notification settings to anonymous for continuity.
     */
    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val user = accountUseCases.getSignedInUser()
            val userId = user?.userId

            // Cancel any sync workers first
            syncScheduler.cancelAllSync()

            // Disable sync
            syncUseCases.setSyncEnabled(false)

            // Reverse-adopt notification settings to anonymous (keeps notifications working)
            if (userId != null) {
                notificationAuthUseCase.handleSignOut(userId)
            }

            // Sign out (local entries/mood colors remain for offline use)
            accountUseCases.signOut()

            _uiState.update {
                it.copy(
                    isUserSignedIn = false,
                    userEmail = null,
                    isLoading = false,
                    shouldNavigateBack = true,
                )
            }
            Timber.i("Sign out successful")
        }
    }

    // ==================== Delete Account ====================

    /**
     * Request account deletion. Shows confirmation dialog.
     */
    fun onDeleteAccountRequested() {
        _uiState.update { it.copy(showDeleteConfirmDialog = true) }
    }

    /**
     * Cancel account deletion request. Hides confirmation dialog.
     */
    fun onDeleteAccountCancelled() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }

    /**
     * Confirm account deletion. Starts the deletion process.
     */
    fun onDeleteAccountConfirmed() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }

        viewModelScope.launch {
            accountUseCases.deleteAccount().collect { progress ->
                _uiState.update { it.copy(deletionProgress = progress) }

                when (progress) {
                    is DeletionProgress.Complete -> {
                        delay(COMPLETION_DELAY_MS)
                        _uiState.update {
                            it.copy(
                                deletionProgress = null,
                                isUserSignedIn = false,
                                userEmail = null,
                                shouldNavigateBack = true,
                            )
                        }
                        Timber.i("Account deletion completed")
                    }

                    is DeletionProgress.RequiresReAuth -> {
                        _uiState.update {
                            it.copy(deletionProgress = null, showReAuthDialog = true)
                        }
                    }

                    is DeletionProgress.Failed -> {
                        _uiState.update {
                            it.copy(deletionProgress = null, error = progress.message)
                        }
                        Timber.w("Account deletion failed: %s", progress.message)
                    }

                    else -> { /* Progress update - no action needed */
                    }
                }
            }
        }
    }

    /**
     * Handle re-authentication completion.
     * Called after user signs in again for account deletion.
     */
    fun onReAuthCompleted(credentialProvider: CredentialProvider) {
        viewModelScope.launch {
            _uiState.update { it.copy(showReAuthDialog = false, isLoading = true) }

            when (val result = accountUseCases.reauthenticate(credentialProvider)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onDeleteAccountConfirmed() // Retry deletion
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = ErrorFormatter.format(result.error, "re-authenticate"),
                        )
                    }
                    Timber.w("Re-authentication failed: %s", result.error)
                }
            }
        }
    }

    /**
     * Dismiss re-authentication dialog without retrying.
     */
    fun onReAuthDismissed() {
        _uiState.update { it.copy(showReAuthDialog = false) }
    }

    /**
     * Dismiss deletion progress dialog.
     */
    fun onDeletionProgressDismissed() {
        _uiState.update { it.copy(deletionProgress = null) }
    }

    /**
     * Reset navigation flag after navigation has occurred.
     */
    fun onNavigationHandled() {
        _uiState.update { it.copy(shouldNavigateBack = false) }
    }

    companion object {
        private const val COMPLETION_DELAY_MS = 500L
    }
}
