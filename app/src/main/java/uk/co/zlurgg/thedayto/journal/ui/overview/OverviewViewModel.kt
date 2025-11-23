package uk.co.zlurgg.thedayto.journal.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.util.DateUtils
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.core.ui.util.launchDebouncedLoading
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.OverviewUseCases
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.journal.domain.model.toEntry
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewAction
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiEvent
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiState
import uk.co.zlurgg.thedayto.journal.ui.overview.util.GreetingConstants
import uk.co.zlurgg.thedayto.journal.ui.overview.util.TimeConstants
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import kotlin.random.Random

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
     * Update greeting based on current time of day with randomized variety.
     *
     * Uses date-based seed to ensure the same greeting is shown throughout the day,
     * preventing the greeting from changing on every screen visit.
     */
    private fun updateGreeting() {
        val hour = LocalTime.now().hour
        val today = LocalDate.now()

        // Use date as seed for consistent greeting throughout the day
        val seed = today.year * 10000 + today.monthValue * 100 + today.dayOfMonth
        val random = Random(seed.toLong())

        // Select time-appropriate greeting list
        val greetingList = when (hour) {
            in TimeConstants.NIGHT_START..TimeConstants.NIGHT_END -> GreetingConstants.NIGHT_GREETINGS  // 12am - 4am
            in TimeConstants.MORNING_START..TimeConstants.MORNING_END -> GreetingConstants.MORNING_GREETINGS  // 5am - 11am
            in TimeConstants.AFTERNOON_START..TimeConstants.AFTERNOON_END -> GreetingConstants.AFTERNOON_GREETINGS  // 12pm - 4pm
            in TimeConstants.EVENING_START..TimeConstants.EVENING_END -> GreetingConstants.EVENING_GREETINGS  // 5pm - 8pm
            else -> GreetingConstants.NIGHT_GREETINGS  // 9pm - 11pm (late evening defaults to night)
        }

        // Pick random greeting from appropriate list
        val greeting = greetingList[random.nextInt(greetingList.size)]

        _uiState.update { it.copy(greeting = greeting) }
    }

    /**
     * Check if today's entry exists and show appropriate dialog
     *
     * For first-time users:
     * - Shows tutorial dialog to explain app usage
     * - First launch marked complete only when tutorial dismissed
     *
     * For returning users without today's entry:
     * - Shows reminder dialog once per day
     */
    private fun checkTodayEntry() {
        viewModelScope.launch {
            val todayEpoch = DateUtils.getTodayStartEpoch()
            val todayEntry = overviewUseCases.getEntryByDate(todayEpoch)

            val hasTodayEntry = todayEntry != null
            _uiState.update { it.copy(hasTodayEntry = hasTodayEntry) }

            // Check if this is first launch
            val isFirstLaunch = overviewUseCases.checkFirstLaunch()

            if (!hasTodayEntry) {
                if (isFirstLaunch) {
                    // First-time users see tutorial (Getting Started)
                    Timber.d("First time user - showing tutorial dialog")
                    _uiState.update { it.copy(showTutorialDialog = true) }
                } else if (!overviewUseCases.checkEntryReminderShownToday()) {
                    // Returning users see entry reminder
                    _uiState.update { it.copy(showEntryReminderDialog = true) }
                }
            }
        }
    }

    /**
     * Load notification settings from preferences.
     *
     * Called during initialization to populate notification state.
     */
    private fun loadNotificationSettings() {
        viewModelScope.launch {
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
        }
    }

    /**
     * Called when notification permission is granted.
     *
     * Updates permission status and keeps settings dialog open so user can
     * enable notifications and set their preferred time.
     */
    fun onNotificationPermissionGranted() {
        _uiState.update {
            it.copy(
                hasNotificationPermission = true,
                notificationsEnabled = true  // Auto-toggle ON in dialog
            )
        }
        Timber.d("Notification permission granted")
    }

    /**
     * Called when notification permission is denied.
     *
     * Checks if permission is permanently denied (Don't ask again).
     * If permanently denied, shows warning to go to Settings.
     * Otherwise, just resets toggle state.
     */
    fun onNotificationPermissionDenied() {
        viewModelScope.launch {
            val shouldShowRationale = overviewUseCases.shouldShowPermissionRationale()
            val hasPermission = overviewUseCases.checkNotificationPermission()

            if (!shouldShowRationale && !hasPermission) {
                // Permanently denied - user selected "Don't ask again" or denied multiple times
                Timber.w("Notification permission permanently denied")
                _uiState.update { it.copy(showNotificationSettingsDialog = false) }
                _uiEvents.emit(OverviewUiEvent.ShowPermissionPermanentlyDeniedDialog)
            } else {
                // Just denied this time - can ask again
                Timber.w("Notification permission denied")
                _uiState.update {
                    it.copy(
                        hasNotificationPermission = false,
                        notificationsEnabled = false  // Reset toggle to OFF
                    )
                }
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

            is OverviewAction.OnMonthChanged -> {
                Timber.d("Month changed to ${action.month}/${action.year}")
                _uiState.update {
                    it.copy(
                        displayedMonth = action.month,
                        displayedYear = action.year
                    )
                }
                // Fetch entries for the new month
                getEntries(_uiState.value.entryOrder)
            }

            is OverviewAction.DeleteEntry -> {
                viewModelScope.launch {
                    val loadingJob = launchDebouncedLoading { isLoading ->
                        _uiState.update { it.copy(isLoading = isLoading) }
                    }

                    try {
                        overviewUseCases.deleteEntry(action.entry.toEntry())
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
                    val loadingJob = launchDebouncedLoading { isLoading ->
                        _uiState.update { it.copy(isLoading = isLoading) }
                    }

                    try {
                        overviewUseCases.restoreEntry(deletedEntry.toEntry())
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

            is OverviewAction.OpenNotificationSettings -> {
                viewModelScope.launch {
                    // Re-check permission status in case user revoked it in system settings
                    val hasPermission = overviewUseCases.checkNotificationPermission()
                    _uiState.update {
                        it.copy(
                            showNotificationSettingsDialog = true,
                            hasNotificationPermission = hasPermission,
                            // If permission was revoked, ensure toggle is off
                            notificationsEnabled = if (hasPermission) it.notificationsEnabled else false
                        )
                    }
                }
            }

            is OverviewAction.DismissNotificationSettings -> {
                _uiState.update { it.copy(showNotificationSettingsDialog = false) }
            }

            is OverviewAction.SaveNotificationSettings -> {
                viewModelScope.launch {
                    try {
                        // Check system notification settings when user tries to enable
                        if (action.enabled) {
                            val systemEnabled = overviewUseCases.checkSystemNotificationsEnabled()
                            if (!systemEnabled) {
                                Timber.w("System notifications are disabled")
                                _uiState.update { it.copy(showNotificationSettingsDialog = false) }
                                _uiEvents.emit(OverviewUiEvent.ShowSystemNotificationWarning)
                                return@launch  // Don't save if system notifications are disabled
                            }
                        }

                        // Save notification settings
                        overviewUseCases.saveNotificationSettings(
                            action.enabled,
                            action.hour,
                            action.minute
                        )

                        _uiState.update {
                            it.copy(
                                notificationsEnabled = action.enabled,
                                notificationHour = action.hour,
                                notificationMinute = action.minute,
                                showNotificationSettingsDialog = false
                            )
                        }

                        // Show confirmation message
                        val timeStr = String.format(Locale.getDefault(), "%02d:%02d", action.hour, action.minute)
                        val message = if (action.enabled) {
                            "Daily reminder set for $timeStr"
                        } else {
                            "Daily reminders disabled"
                        }
                        _uiEvents.emit(OverviewUiEvent.ShowSnackbar(message))

                    } catch (e: Exception) {
                        Timber.e(e, "Failed to save notification settings")
                        _uiEvents.emit(
                            OverviewUiEvent.ShowSnackbar(
                                message = "Failed to save notification settings: ${e.message}"
                            )
                        )
                    }
                }
            }

            is OverviewAction.RequestSignOut -> {
                viewModelScope.launch {
                    _uiEvents.emit(OverviewUiEvent.ShowSignOutDialog)
                }
            }

            is OverviewAction.RequestShowTutorial -> {
                _uiState.update { it.copy(showTutorialDialog = true) }
            }

            is OverviewAction.RequestShowHelp -> {
                viewModelScope.launch {
                    _uiEvents.emit(OverviewUiEvent.ShowHelpDialog)
                }
            }

            is OverviewAction.RequestShowAbout -> {
                viewModelScope.launch {
                    _uiEvents.emit(OverviewUiEvent.ShowAboutDialog)
                }
            }

            is OverviewAction.DismissTutorial -> {
                viewModelScope.launch {
                    Timber.d("Tutorial dismissed - marking first launch complete")
                    // Mark first launch complete only when user actually dismisses tutorial
                    overviewUseCases.markFirstLaunchComplete()
                    _uiState.update { it.copy(showTutorialDialog = false) }

                    // After tutorial is dismissed, check if entry reminder should be shown
                    // This creates the flow: Tutorial → Entry Reminder → Create Entry
                    val todayEpoch = DateUtils.getTodayStartEpoch()
                    val todayEntry = overviewUseCases.getEntryByDate(todayEpoch)

                    if (todayEntry == null && !overviewUseCases.checkEntryReminderShownToday()) {
                        Timber.d("Tutorial dismissed, showing entry reminder")
                        _uiState.update { it.copy(showEntryReminderDialog = true) }
                    }
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
     * Get entries for the currently displayed month.
     *
     * Filters entries at the database level for optimal performance.
     * Automatically updates when database changes (new/updated/deleted entries).
     */
    private fun getEntries(entryOrder: EntryOrder) {
        getEntriesJob?.cancel()
        val currentState = _uiState.value
        getEntriesJob = overviewUseCases.getEntriesForMonth(
            month = currentState.displayedMonth,
            year = currentState.displayedYear,
            entryOrder = entryOrder
        )
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
