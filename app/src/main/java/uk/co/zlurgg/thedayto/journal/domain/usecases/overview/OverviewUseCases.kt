package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

data class OverviewUseCases(
    val getEntries: GetEntriesUseCase,
    val deleteEntry: DeleteEntryUseCase,
    val restoreEntry: RestoreEntryUseCase,
    val getEntryByDate: GetEntryByDateUseCase,
    val updateEntryUseCase: UpdateEntryUseCase,
    val checkEntryReminderShownToday: CheckEntryReminderShownTodayUseCase,
    val markEntryReminderShownToday: MarkEntryReminderShownTodayUseCase,
    val getNotificationSettings: uk.co.zlurgg.thedayto.core.domain.usecases.notifications.GetNotificationSettingsUseCase,
    val saveNotificationSettings: uk.co.zlurgg.thedayto.core.domain.usecases.notifications.SaveNotificationSettingsUseCase,
    val checkNotificationPermission: uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckNotificationPermissionUseCase
)