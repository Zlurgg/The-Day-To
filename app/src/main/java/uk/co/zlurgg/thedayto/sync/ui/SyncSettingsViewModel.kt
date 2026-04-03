package uk.co.zlurgg.thedayto.sync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.auth.domain.model.CredentialProvider
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.auth.domain.usecases.DevSignInUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignOutUseCase
import uk.co.zlurgg.thedayto.core.domain.error.ErrorFormatter
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SeedDefaultMoodColorsUseCase
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler
import uk.co.zlurgg.thedayto.sync.domain.model.SyncState
import uk.co.zlurgg.thedayto.sync.domain.repository.SyncRepository
import uk.co.zlurgg.thedayto.sync.domain.usecase.SyncUseCases

/**
 * ViewModel for the Account screen.
 *
 * Manages sign-in/sign-out state and triggers sync operations.
 * Sync is automatically enabled when signed in.
 */
class SyncSettingsViewModel(
    private val syncUseCases: SyncUseCases,
    private val authRepository: AuthRepository,
    private val syncScheduler: SyncScheduler,
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val preferencesRepository: PreferencesRepository,
    private val syncRepository: SyncRepository,
    private val seedDefaultMoodColorsUseCase: SeedDefaultMoodColorsUseCase,
    private val devSignInUseCase: DevSignInUseCase? = null
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
                val user = authRepository.getSignedInUser()
                val isSignedIn = user != null
                val lastSync = syncUseCases.getLastSyncTimestamp()

                _uiState.update {
                    it.copy(
                        isUserSignedIn = isSignedIn,
                        userEmail = user?.username,
                        lastSyncTimestamp = lastSync,
                        isDevSignInAvailable = devSignInUseCase?.isAvailable() == true,
                        isLoading = false
                    )
                }

                Timber.d("Account state loaded: signedIn=$isSignedIn")
            } catch (e: Exception) {
                Timber.e(e, "Error loading account state")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load account state"
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

            when (val result = signInUseCase(credentialProvider)) {
                is Result.Success -> {
                    handleSignInSuccess()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSigningIn = false,
                            error = ErrorFormatter.format(result.error, "sign in")
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
        val useCase = devSignInUseCase ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true, error = null) }

            when (val result = useCase(DEV_EMAIL, DEV_PASSWORD)) {
                is Result.Success -> {
                    handleSignInSuccess()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSigningIn = false,
                            error = ErrorFormatter.format(result.error, "dev sign in")
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
     */
    private suspend fun handleSignInSuccess() {
        val user = authRepository.getSignedInUser() ?: return
        val userId = user.userId

        // Auto-enable sync on sign-in
        preferencesRepository.setSyncEnabled(true)

        // Clear data from any other user (user isolation)
        syncRepository.clearOtherUserData(userId)

        // Adopt orphaned data (userId = null) - this is data created before first sign-in
        syncRepository.adoptOrphanedData(userId)

        // Mark any LOCAL_ONLY data as PENDING_SYNC so it gets uploaded
        syncRepository.markLocalDataForSync()

        // Start background sync and trigger immediate first sync
        syncScheduler.startPeriodicSync()
        syncScheduler.requestImmediateSync()

        val lastSync = syncUseCases.getLastSyncTimestamp()
        _uiState.update {
            it.copy(
                isUserSignedIn = true,
                isSigningIn = false,
                userEmail = user.username,
                lastSyncTimestamp = lastSync
            )
        }
        Timber.i("Sign in successful: %s", user.username)
    }

    /**
     * Sign out and disable sync.
     * Called when user taps Sign Out button in Account settings.
     * Clears synced data for privacy - user can re-download on next sign-in.
     */
    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val user = authRepository.getSignedInUser()
                val userId = user?.userId

                // Cancel any sync workers first
                syncScheduler.cancelAllSync()

                // Disable sync
                preferencesRepository.setSyncEnabled(false)

                // Clear this user's synced data (will re-download on next sign-in)
                if (userId != null) {
                    syncRepository.clearUserData(userId)
                }

                // Restore default mood colors for offline use
                seedDefaultMoodColorsUseCase.reseed()

                // Sign out
                signOutUseCase()

                _uiState.update {
                    it.copy(
                        isUserSignedIn = false,
                        userEmail = null,
                        isLoading = false
                    )
                }
                Timber.i("Sign out successful")
            } catch (e: Exception) {
                Timber.e(e, "Error signing out")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to sign out"
                    )
                }
            }
        }
    }

    companion object {
        private const val DEV_EMAIL = "test@example.com"
        private const val DEV_PASSWORD = "password123"
    }
}
