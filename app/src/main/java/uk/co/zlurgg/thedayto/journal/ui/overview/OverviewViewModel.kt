package uk.co.zlurgg.thedayto.journal.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.zlurgg.thedayto.auth.data.service.GoogleAuthUiClient
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.OverviewUseCases
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewAction
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiEvent
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiState
import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository
import java.time.LocalTime

class OverviewViewModel(
    private val entryUseCase: OverviewUseCases,
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val authStateRepository: AuthStateRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    // Single source of truth for UI state
    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState = _uiState.asStateFlow()

    // One-time UI events
    private val _uiEvents = MutableSharedFlow<OverviewUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private var getEntriesJob: Job? = null

    init {
        getEntries(EntryOrder.Date(OrderType.Descending))
        updateGreeting()
    }

    /**
     * Update greeting based on current time of day
     */
    private fun updateGreeting() {
        val hour = LocalTime.now().hour
        val greeting = when (hour) {
            in 0..4 -> "Good night"
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
        _uiState.update { it.copy(greeting = greeting) }
    }

    fun onAction(action: OverviewAction) {
        when (action) {
            is OverviewAction.Order -> {
                // Check if order actually changed to avoid unnecessary fetches
                if (_uiState.value.entryOrder::class == action.entryOrder::class &&
                    _uiState.value.entryOrder.orderType == action.entryOrder.orderType
                ) {
                    return
                }
                getEntries(entryOrder = action.entryOrder)
            }

            is OverviewAction.DeleteEntry -> {
                viewModelScope.launch {
                    // Debounced loading: only show if operation takes > 150ms
                    val loadingJob = launch {
                        delay(150)
                        _uiState.update { it.copy(isLoading = true) }
                    }

                    try {
                        entryUseCase.deleteEntry(action.entry)
                        loadingJob.cancel()
                        _uiState.update {
                            it.copy(
                                recentlyDeletedEntry = action.entry,
                                isLoading = false
                            )
                        }
                    } catch (e: Exception) {
                        loadingJob.cancel()
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(
                            OverviewUiEvent.ShowSnackbar(
                                message = "Failed to delete entry: ${e.message}"
                            )
                        )
                    }
                }
            }

            is OverviewAction.RestoreEntry -> {
                viewModelScope.launch {
                    val deletedEntry = _uiState.value.recentlyDeletedEntry ?: return@launch

                    // Debounced loading: only show if operation takes > 150ms
                    val loadingJob = launch {
                        delay(150)
                        _uiState.update { it.copy(isLoading = true) }
                    }

                    try {
                        entryUseCase.restoreEntry(deletedEntry)
                        loadingJob.cancel()
                        _uiState.update {
                            it.copy(
                                recentlyDeletedEntry = null,
                                isLoading = false
                            )
                        }
                    } catch (e: Exception) {
                        loadingJob.cancel()
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(
                            OverviewUiEvent.ShowSnackbar(
                                message = "Failed to restore entry: ${e.message}"
                            )
                        )
                    }
                }
            }

            is OverviewAction.RequestNotificationPermission -> {
                viewModelScope.launch {
                    _uiEvents.emit(OverviewUiEvent.RequestNotificationPermission)
                }
            }

            is OverviewAction.SignOut -> {
                viewModelScope.launch {
                    // Debounced loading: only show if operation takes > 150ms
                    val loadingJob = launch {
                        delay(150)
                        _uiState.update { it.copy(isLoading = true) }
                    }

                    try {
                        // Sign out from Google Auth
                        googleAuthUiClient.signOut()

                        // Clear sign-in state
                        authStateRepository.setSignedInState(false)

                        loadingJob.cancel()
                        _uiState.update { it.copy(isLoading = false) }

                        // Navigate to sign-in screen
                        _uiEvents.emit(OverviewUiEvent.NavigateToSignIn)
                    } catch (e: Exception) {
                        loadingJob.cancel()
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(
                            OverviewUiEvent.ShowSnackbar(
                                message = "Failed to sign out: ${e.message}"
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Called after notification permission is granted.
     * Sets up daily notification scheduling.
     */
    fun onNotificationPermissionGranted() {
        notificationRepository.setupDailyNotificationIfNeeded()
    }

    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(): Boolean {
        return notificationRepository.hasNotificationPermission()
    }

    private fun getEntries(entryOrder: EntryOrder) {
        getEntriesJob?.cancel()
        getEntriesJob = entryUseCase.getEntries(entryOrder)
            .onEach { entries ->
                _uiState.update {
                    it.copy(
                        entries = entries,
                        entryOrder = entryOrder
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
