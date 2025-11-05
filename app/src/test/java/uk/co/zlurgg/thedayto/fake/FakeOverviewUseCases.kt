package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckNotificationPermissionUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckSystemNotificationsEnabledUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.GetNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.SaveNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.ShouldShowPermissionRationaleUseCase
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.CheckEntryReminderShownTodayUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.DeleteEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.GetEntriesUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.GetEntryByDateUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.MarkEntryReminderShownTodayUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.OverviewUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.RestoreEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.UpdateEntryUseCase

/**
 * Creates a fake OverviewUseCases instance for testing.
 *
 * Uses real use case implementations with fake repositories,
 * following the project's testing pattern.
 */
fun createFakeOverviewUseCases(
    preferencesRepository: FakePreferencesRepository,
    notificationRepository: FakeNotificationRepository,
    entryRepository: EntryRepository = FakeEntryRepository()
): OverviewUseCases {

    // Create real notification use cases with fake repositories
    val getNotificationSettings = GetNotificationSettingsUseCase(preferencesRepository)
    val saveNotificationSettings = SaveNotificationSettingsUseCase(
        preferencesRepository,
        notificationRepository
    )
    val checkNotificationPermission = CheckNotificationPermissionUseCase(notificationRepository)
    val checkSystemNotificationsEnabled = CheckSystemNotificationsEnabledUseCase(notificationRepository)
    val shouldShowPermissionRationale = ShouldShowPermissionRationaleUseCase(notificationRepository)

    val checkEntryReminderShownToday = CheckEntryReminderShownTodayUseCase(preferencesRepository)
    val markEntryReminderShownToday = MarkEntryReminderShownTodayUseCase(preferencesRepository)

    // Create real entry use cases with fake repository
    val getEntries = GetEntriesUseCase(entryRepository)
    val deleteEntry = DeleteEntryUseCase(entryRepository)
    val restoreEntry = RestoreEntryUseCase(entryRepository)
    val getEntryByDate = GetEntryByDateUseCase(entryRepository)
    val updateEntry = UpdateEntryUseCase(entryRepository)

    return OverviewUseCases(
        getEntries = getEntries,
        deleteEntry = deleteEntry,
        restoreEntry = restoreEntry,
        getEntryByDate = getEntryByDate,
        updateEntryUseCase = updateEntry,
        checkEntryReminderShownToday = checkEntryReminderShownToday,
        markEntryReminderShownToday = markEntryReminderShownToday,
        getNotificationSettings = getNotificationSettings,
        saveNotificationSettings = saveNotificationSettings,
        checkNotificationPermission = checkNotificationPermission,
        checkSystemNotificationsEnabled = checkSystemNotificationsEnabled,
        shouldShowPermissionRationale = shouldShowPermissionRationale
    )
}
