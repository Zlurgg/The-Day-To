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
import timber.log.Timber
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
        loadNotificationSettings()
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

            is OverviewAction.DismissNotificationConfirmDialog -> {
                _uiState.update { it.copy(showNotificationConfirmDialog = false) }
            }

            is OverviewAction.OpenNotificationSettings -> {
                _uiState.update {
                    it.copy(
                        showNotificationConfirmDialog = false,
                        showNotificationSettingsDialog = true
                    )
                }
            }

            is OverviewAction.DismissNotificationSettings -> {
                _uiState.update { it.copy(showNotificationSettingsDialog = false) }
            }

            is OverviewAction.SaveNotificationSettings -> {
                viewModelScope.launch {
                    try {
                        // Save settings and update notification schedule
                        overviewUseCases.saveNotificationSettings(
                            enabled = action.enabled,
                            hour = action.hour,
                            minute = action.minute
                        )

                        // Update UI state
                        _uiState.update {
                            it.copy(
                                notificationsEnabled = action.enabled,
                                notificationHour = action.hour,
                                notificationMinute = action.minute,
                                showNotificationSettingsDialog = false
                            )
                        }

                        // Show confirmation snackbar
                        val timeStr = "${action.hour.toString().padStart(2, '0')}:${action.minute.toString().padStart(2, '0')}"
                        val message = if (action.enabled) {
                            "Notifications enabled for $timeStr"
                        } else {
                            "Notifications disabled"
                        }
                        _uiEvents.emit(OverviewUiEvent.ShowSnackbar(message))
                    } catch (e: Exception) {
                        _uiEvents.emit(
                            OverviewUiEvent.ShowSnackbar("Failed to save notification settings")
                        )
                    }
                }
            }
        }
    }

    /**
     * Called after notification permission is granted.
     * Sets up daily notification scheduling and shows confirm dialog.
     */
    fun onNotificationPermissionGranted() {
        viewModelScope.launch {
            // Enable notifications with default time (9:00 AM)
            overviewUseCases.saveNotificationSettings(
                enabled = true,
                hour = 9,
                minute = 0
            )

            // Update state and show confirm dialog
            _uiState.update {
                it.copy(
                    notificationsEnabled = true,
                    notificationHour = 9,
                    notificationMinute = 0,
                    hasNotificationPermission = true,
                    showNotificationConfirmDialog = true
                )
            }
        }
    }

    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(): Boolean {
        return overviewUseCases.checkNotificationPermission()
    }

    /**
     * Load notification settings from preferences on init
     */
    private fun loadNotificationSettings() {
        viewModelScope.launch {
            try {
                val settings = overviewUseCases.getNotificationSettings()
                val hasPermission = overviewUseCases.checkNotificationPermission()

                _uiState.update {
                    it.copy(
                        notificationsEnabled = settings.enabled,
                        notificationHour = settings.hour,
                        notificationMinute = settings.minute,
                        hasNotificationPermission = hasPermission
                    )
                }
            } catch (e: Exception) {
                // Log error but don't crash - notifications are not critical
                Timber.e(e, "Failed to load notification settings")
            }
        }
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
