package uk.co.zlurgg.thedayto.journal.ui.overview.state

import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor

sealed interface OverviewAction {
    data class Order(val entryOrder: EntryOrder) : OverviewAction
    data class DeleteEntry(val entry: EntryWithMoodColor) : OverviewAction
    data object RestoreEntry : OverviewAction
    data object ClearRecentlyDeleted : OverviewAction
    data object RetryLoadEntries : OverviewAction
    data object DismissLoadError : OverviewAction

    // Calendar actions
    data class OnMonthChanged(val month: Int, val year: Int) : OverviewAction

    // Notification actions
    data object RequestNotificationPermission : OverviewAction
    data object OpenNotificationSettings : OverviewAction
    data object DismissNotificationSettings : OverviewAction
    data class SaveNotificationSettings(val enabled: Boolean, val hour: Int, val minute: Int) : OverviewAction

    // Other actions
    data object RequestSignOut : OverviewAction
    data object RequestShowTutorial : OverviewAction
    data object RequestShowHelp : OverviewAction
    data object RequestShowAbout : OverviewAction
    data object DismissTutorial : OverviewAction
    data object DismissEntryReminder : OverviewAction
    data object CreateTodayEntry : OverviewAction
    data object CreateNewEntry : OverviewAction

    // Update actions
    data object CheckForUpdates : OverviewAction
    data object DownloadUpdate : OverviewAction
    data object DismissUpdate : OverviewAction
    data object DismissUpToDate : OverviewAction
}
