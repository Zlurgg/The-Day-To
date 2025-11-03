package uk.co.zlurgg.thedayto.journal.ui.overview.state

import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.domain.model.Entry

/**
 * UI state for the Overview screen.
 *
 * ARCHITECTURAL NOTE: Notification Settings State
 * ================================================
 * Notification state is managed in this ViewModel (rather than a separate ViewModel) because:
 *
 * 1. Google's MAD Recommendation: ONE ViewModel per screen/destination
 *    - OverviewScreen is the destination, so it has one ViewModel
 *    - Multiple ViewModels per screen is considered an anti-pattern
 *
 * 2. Material Design 3 Guidelines: Dialogs for simple settings
 *    - Notification settings are accessed via dialogs FROM this screen
 *    - MD3 recommends dialogs (not dedicated screens) for simple settings (≤5 fields)
 *
 * 3. Consistency: Matches other dialog patterns in this screen
 *    - SignOutDialog, TutorialDialog, EntryReminderDialog all work the same way
 *    - Their state lives in OverviewViewModel, not separate ViewModels
 *
 * 4. Separation of Concerns is Maintained:
 *    - Notification USE CASES remain in core/domain/usecases/notifications/
 *    - Notification DOMAIN MODELS remain in core/domain/model/
 *    - Only the UI STATE for this screen's dialogs lives here
 *
 * 5. Avoids Anti-Pattern: No multiple ViewModels per screen
 *    - Previous implementation had both OverviewViewModel + NotificationSettingsViewModel
 *    - This violated Google's recommendation and complicated lifecycle management
 *
 * The notification infrastructure (use cases, repository, worker) remains in core/
 * as cross-cutting concerns, but the UI state belongs to this screen's ViewModel.
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

    // Notification settings state (see architectural note above)
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val hasNotificationPermission: Boolean = false,
    val showNotificationConfirmDialog: Boolean = false,
    val showNotificationSettingsDialog: Boolean = false
)
