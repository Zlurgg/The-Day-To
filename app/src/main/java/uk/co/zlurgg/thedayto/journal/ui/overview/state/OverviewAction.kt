package uk.co.zlurgg.thedayto.journal.ui.overview.state

import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.journal.domain.model.Entry

sealed interface OverviewAction {
    data class Order(val entryOrder: EntryOrder) : OverviewAction
    data class DeleteEntry(val entry: Entry) : OverviewAction
    data object RestoreEntry : OverviewAction
    data object RequestNotificationPermission : OverviewAction
    data object RequestSignOut : OverviewAction
    data object DismissEntryReminder : OverviewAction
    data object CreateTodayEntry : OverviewAction
    data object CreateNewEntry : OverviewAction
}
