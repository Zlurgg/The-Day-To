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
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.core.domain.util.DateUtils
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.OverviewUseCases
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewAction
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiEvent
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiState
import java.time.LocalTime

class OverviewViewModel(
    private val overviewUseCases: OverviewUseCases
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
        checkTodayEntry()
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

    /**
     * Check if today's entry exists and show reminder dialog if needed
     *
     * Shows the reminder dialog once per day if:
     * - No entry exists for today
     * - Reminder hasn't been shown today yet
     */
    private fun checkTodayEntry() {
        viewModelScope.launch {
            val todayEpoch = DateUtils.getTodayStartEpoch()
            val todayEntry = overviewUseCases.getEntryByDate(todayEpoch)

            val hasTodayEntry = todayEntry != null
            _uiState.update { it.copy(hasTodayEntry = hasTodayEntry) }

            // Show reminder dialog if no entry + haven't shown today
            if (!hasTodayEntry && !overviewUseCases.checkEntryReminderShownToday()) {
                _uiState.update { it.copy(showEntryReminderDialog = true) }
            }
        }
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
                        overviewUseCases.deleteEntry(action.entry)
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
                        overviewUseCases.restoreEntry(deletedEntry)
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

            is OverviewAction.RequestSignOut -> {
                viewModelScope.launch {
                    _uiEvents.emit(OverviewUiEvent.ShowSignOutDialog)
                }
            }

            is OverviewAction.RequestShowTutorial -> {
                viewModelScope.launch {
                    _uiEvents.emit(OverviewUiEvent.ShowTutorialDialog)
                }
            }

            is OverviewAction.DismissEntryReminder -> {
                viewModelScope.launch {
                    overviewUseCases.markEntryReminderShownToday()
                    _uiState.update { it.copy(showEntryReminderDialog = false) }
                }
            }

            is OverviewAction.CreateTodayEntry,
            is OverviewAction.CreateNewEntry -> {
                viewModelScope.launch {
                    _uiEvents.emit(OverviewUiEvent.NavigateToEditor(entryId = null))
                }
            }
        }
    }

    /**
     * Called after notification permission is granted.
     * Sets up daily notification scheduling.
     */
    fun onNotificationPermissionGranted() {
        overviewUseCases.setupNotification()
    }

    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(): Boolean {
        return overviewUseCases.checkNotificationPermission()
    }

    private fun getEntries(entryOrder: EntryOrder) {
        getEntriesJob?.cancel()
        getEntriesJob = overviewUseCases.getEntries(entryOrder)
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
