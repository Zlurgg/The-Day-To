package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckNotificationPermissionUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckSystemNotificationsEnabledUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.GetNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.SaveNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.ShouldShowPermissionRationaleUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntriesForMonthUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntriesUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryByDateUseCase

data class OverviewUseCases(
    val getEntries: GetEntriesUseCase,
    val getEntriesForMonth: GetEntriesForMonthUseCase,
    val deleteEntry: DeleteEntryUseCase,
    val restoreEntry: RestoreEntryUseCase,
    val getEntryByDate: GetEntryByDateUseCase,
    val updateEntryUseCase: UpdateEntryUseCase,
    val checkEntryReminderShownToday: CheckEntryReminderShownTodayUseCase,
    val markEntryReminderShownToday: MarkEntryReminderShownTodayUseCase,
    val checkFirstLaunch: CheckFirstLaunchUseCase,
    val markFirstLaunchComplete: MarkFirstLaunchCompleteUseCase,
    val getNotificationSettings: GetNotificationSettingsUseCase,
    val saveNotificationSettings: SaveNotificationSettingsUseCase,
    val checkNotificationPermission: CheckNotificationPermissionUseCase,
    val checkSystemNotificationsEnabled: CheckSystemNotificationsEnabledUseCase,
    val shouldShowPermissionRationale: ShouldShowPermissionRationaleUseCase
)