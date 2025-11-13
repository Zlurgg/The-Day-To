package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntriesUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryByDateUseCase

data class OverviewUseCases(
    val getEntries: GetEntriesUseCase,
    val deleteEntry: DeleteEntryUseCase,
    val restoreEntry: RestoreEntryUseCase,
    val getEntryByDate: GetEntryByDateUseCase,
    val updateEntryUseCase: UpdateEntryUseCase,
    val checkEntryReminderShownToday: CheckEntryReminderShownTodayUseCase,
    val markEntryReminderShownToday: MarkEntryReminderShownTodayUseCase,
    val checkFirstLaunch: CheckFirstLaunchUseCase,
    val markFirstLaunchComplete: MarkFirstLaunchCompleteUseCase,
    val getNotificationSettings: uk.co.zlurgg.thedayto.core.domain.usecases.notifications.GetNotificationSettingsUseCase,
    val saveNotificationSettings: uk.co.zlurgg.thedayto.core.domain.usecases.notifications.SaveNotificationSettingsUseCase,
    val checkNotificationPermission: uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckNotificationPermissionUseCase,
    val checkSystemNotificationsEnabled: uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckSystemNotificationsEnabledUseCase,
    val shouldShowPermissionRationale: uk.co.zlurgg.thedayto.core.domain.usecases.notifications.ShouldShowPermissionRationaleUseCase
)