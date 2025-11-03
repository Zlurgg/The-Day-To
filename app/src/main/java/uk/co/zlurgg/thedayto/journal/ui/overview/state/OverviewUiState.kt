package uk.co.zlurgg.thedayto.journal.ui.overview.state

import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.domain.model.Entry

data class OverviewUiState(
    val entries: List<Entry> = emptyList(),
    val entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending),
    val entryMade: Boolean = false,
    val recentlyDeletedEntry: Entry? = null,
    val isLoading: Boolean = false,
    val greeting: String = "",
    val showEntryReminderDialog: Boolean = false,
    val hasTodayEntry: Boolean = true,
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val hasNotificationPermission: Boolean = false,
    val showNotificationConfirmDialog: Boolean = false,
    val showNotificationSettingsDialog: Boolean = false
)
