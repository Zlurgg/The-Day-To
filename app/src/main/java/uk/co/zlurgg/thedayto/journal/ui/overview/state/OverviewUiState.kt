package uk.co.zlurgg.thedayto.journal.ui.overview.state

import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.domain.model.Entry

/**
 * UI state for the Overview screen following Google's MAD single ViewModel per screen pattern.
 * Notification settings state is included here as it's managed via dialogs on this screen.
 */
data class OverviewUiState(
    // Entry-related state
    val entries: List<Entry> = emptyList(),
    val entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending),
    val entryMade: Boolean = false,
    val recentlyDeletedEntry: Entry? = null,
    val isLoading: Boolean = false,

    // Screen-level state
    val greeting: String = "",
    val hasTodayEntry: Boolean = true,

    // Dialog state
    val showEntryReminderDialog: Boolean = false,

    // Notification settings state
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val hasNotificationPermission: Boolean = false,
    val showNotificationSettingsDialog: Boolean = false
)
