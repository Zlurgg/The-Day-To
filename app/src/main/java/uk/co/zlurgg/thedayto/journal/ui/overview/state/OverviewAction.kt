package uk.co.zlurgg.thedayto.journal.ui.overview.state

import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.journal.domain.model.Entry

sealed interface OverviewAction {
    data class Order(val entryOrder: EntryOrder) : OverviewAction
    data class DeleteEntry(val entry: Entry) : OverviewAction
    data object RestoreEntry : OverviewAction

    // Notification actions
    data object RequestNotificationPermission : OverviewAction
    data object DismissNotificationConfirmDialog : OverviewAction
    data object OpenNotificationSettings : OverviewAction
    data object DismissNotificationSettings : OverviewAction
    data class SaveNotificationSettings(val enabled: Boolean, val hour: Int, val minute: Int) : OverviewAction

    // Other actions
    data object RequestSignOut : OverviewAction
    data object RequestShowTutorial : OverviewAction
    data object DismissEntryReminder : OverviewAction
    data object CreateTodayEntry : OverviewAction
    data object CreateNewEntry : OverviewAction
}
