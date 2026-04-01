package uk.co.zlurgg.thedayto.sync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.error.ErrorFormatter
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler
import uk.co.zlurgg.thedayto.sync.domain.model.SyncState
import uk.co.zlurgg.thedayto.sync.domain.usecase.SyncUseCases

/**
 * ViewModel for the Sync Settings screen.
 *
 * Manages sync enable/disable state and triggers sync operations.
 */
class SyncSettingsViewModel(
    private val syncUseCases: SyncUseCases,
    private val authRepository: AuthRepository,
    private val syncScheduler: SyncScheduler
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
                val isSignedIn = authRepository.getSignedInUser() != null
                val isSyncEnabled = syncUseCases.checkSyncEnabled()
                val lastSync = syncUseCases.getLastSyncTimestamp()

                _uiState.update {
                    it.copy(
                        isUserSignedIn = isSignedIn,
                        isSyncEnabled = isSyncEnabled,
                        lastSyncTimestamp = lastSync,
                        isLoading = false
                    )
                }

                Timber.d("Sync settings loaded: signedIn=$isSignedIn, enabled=$isSyncEnabled")
            } catch (e: Exception) {
                Timber.e(e, "Error loading sync settings")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load sync settings"
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

    fun onSyncToggled(enabled: Boolean) {
        if (enabled) {
            enableSync()
        } else {
            disableSync()
        }
    }

    private fun enableSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = syncUseCases.enableSync()) {
                is Result.Success -> {
                    syncScheduler.startPeriodicSync()
                    val lastSync = syncUseCases.getLastSyncTimestamp()
                    _uiState.update {
                        it.copy(
                            isSyncEnabled = true,
                            lastSyncTimestamp = lastSync,
                            isLoading = false
                        )
                    }
                    Timber.i("Sync enabled successfully")
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = ErrorFormatter.format(result.error, "enable sync")
                        )
                    }
                    Timber.w("Failed to enable sync: ${result.error}")
                }
            }
        }
    }

    private fun disableSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                syncUseCases.disableSync()
                syncScheduler.stopPeriodicSync()
                _uiState.update {
                    it.copy(
                        isSyncEnabled = false,
                        isLoading = false
                    )
                }
                Timber.i("Sync disabled successfully")
            } catch (e: Exception) {
                Timber.e(e, "Error disabling sync")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to disable sync"
                    )
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
}
