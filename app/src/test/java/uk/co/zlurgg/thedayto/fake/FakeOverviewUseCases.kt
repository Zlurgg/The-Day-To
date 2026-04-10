package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckNotificationPermissionUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckSystemNotificationsEnabledUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.GetNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.SaveNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.SetupDailyNotificationUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.ShouldShowPermissionRationaleUseCase
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.CheckEntryReminderShownTodayUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.CheckFirstLaunchUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.DeleteEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.MarkEntryReminderShownTodayUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.MarkFirstLaunchCompleteUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.OverviewUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.RestoreEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.UpdateEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntriesForMonthUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntriesUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryByDateUseCase
import uk.co.zlurgg.thedayto.notification.domain.repository.NotificationSettingsRepository
import uk.co.zlurgg.thedayto.notification.domain.scheduler.NotificationScheduler

/**
 * Creates a fake OverviewUseCases instance for testing.
 *
 * Uses real use case implementations with fake repositories,
 * following the project's testing pattern.
 */
fun createFakeOverviewUseCases(
    preferencesRepository: FakePreferencesRepository,
    notificationScheduler: NotificationScheduler,
    notificationSettingsRepository: NotificationSettingsRepository = FakeNotificationSettingsRepository(),
    authRepository: AuthRepository = FakeAuthRepository(),
    entryRepository: EntryRepository = FakeEntryRepository(),
): OverviewUseCases {

    // Create real notification use cases with fake dependencies
    val getNotificationSettings = GetNotificationSettingsUseCase(
        settingsRepository = notificationSettingsRepository,
        authRepository = authRepository,
    )
    val saveNotificationSettings = SaveNotificationSettingsUseCase(
        settingsRepository = notificationSettingsRepository,
        scheduler = notificationScheduler,
        authRepository = authRepository,
    )
    val checkNotificationPermission = CheckNotificationPermissionUseCase(notificationScheduler)
    val checkSystemNotificationsEnabled = CheckSystemNotificationsEnabledUseCase(notificationScheduler)
    val shouldShowPermissionRationale = ShouldShowPermissionRationaleUseCase(notificationScheduler)
    val setupDailyNotification = SetupDailyNotificationUseCase(notificationScheduler)

    val checkEntryReminderShownToday = CheckEntryReminderShownTodayUseCase(preferencesRepository)
    val markEntryReminderShownToday = MarkEntryReminderShownTodayUseCase(preferencesRepository)
    val checkFirstLaunch = CheckFirstLaunchUseCase(preferencesRepository)
    val markFirstLaunchComplete = MarkFirstLaunchCompleteUseCase(preferencesRepository)

    // Create real entry use cases with fake repository
    val getEntries = GetEntriesUseCase(entryRepository)
    val getEntriesForMonth = GetEntriesForMonthUseCase(entryRepository)
    val deleteEntry = DeleteEntryUseCase(entryRepository)
    val restoreEntry = RestoreEntryUseCase(entryRepository)
    val getEntryByDate = GetEntryByDateUseCase(entryRepository)
    val updateEntry = UpdateEntryUseCase(entryRepository)

    return OverviewUseCases(
        getEntries = getEntries,
        getEntriesForMonth = getEntriesForMonth,
        deleteEntry = deleteEntry,
        restoreEntry = restoreEntry,
        getEntryByDate = getEntryByDate,
        updateEntryUseCase = updateEntry,
        checkEntryReminderShownToday = checkEntryReminderShownToday,
        markEntryReminderShownToday = markEntryReminderShownToday,
        checkFirstLaunch = checkFirstLaunch,
        markFirstLaunchComplete = markFirstLaunchComplete,
        getNotificationSettings = getNotificationSettings,
        saveNotificationSettings = saveNotificationSettings,
        checkNotificationPermission = checkNotificationPermission,
        checkSystemNotificationsEnabled = checkSystemNotificationsEnabled,
        shouldShowPermissionRationale = shouldShowPermissionRationale,
        setupDailyNotification = setupDailyNotification,
    )
}
