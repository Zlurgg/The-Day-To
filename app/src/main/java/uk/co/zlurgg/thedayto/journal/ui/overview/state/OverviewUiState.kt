package uk.co.zlurgg.thedayto.journal.ui.overview.state

import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import java.time.LocalDate

/**
 * UI state for the Overview screen following Google's MAD single ViewModel per screen pattern.
 * Notification settings state is included here as it's managed via dialogs on this screen.
 */
data class OverviewUiState(
    // Entry-related state
    // Contains entries for the currently displayed month (filtered at database level)
    val entries: List<EntryWithMoodColor> = emptyList(),
    val entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending),
    val entryMade: Boolean = false,
    val recentlyDeletedEntry: EntryWithMoodColor? = null,
    val isLoading: Boolean = false,
    val loadError: String? = null,

    // Calendar state - tracks currently displayed month/year
    val displayedMonth: Int = LocalDate.now().monthValue,
    val displayedYear: Int = LocalDate.now().year,

    // Screen-level state
    val greeting: String = "",
    val hasTodayEntry: Boolean = true,

    // Dialog state
    val showEntryReminderDialog: Boolean = false,
    val showTutorialDialog: Boolean = false,

    // Notification settings state
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val hasNotificationPermission: Boolean = false,
    val showNotificationSettingsDialog: Boolean = false
)
