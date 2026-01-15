package uk.co.zlurgg.thedayto.fake

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
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntriesUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntriesForMonthUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryByDateUseCase
import io.github.zlurgg.update.domain.model.UpdateConfig
import io.github.zlurgg.update.domain.usecases.CheckForUpdateUseCase
import io.github.zlurgg.update.domain.usecases.DismissUpdateUseCase
import io.github.zlurgg.update.domain.usecases.DownloadUpdateUseCase
import io.github.zlurgg.update.domain.usecases.GetCurrentVersionInfoUseCase

private const val DEFAULT_CURRENT_VERSION = "1.0.0"
private val DEFAULT_UPDATE_CONFIG = UpdateConfig(
    gitHubOwner = "TestOwner",
    gitHubRepo = "TestRepo",
    appName = "test-app"
)

/**
 * Creates a fake OverviewUseCases instance for testing.
 *
 * Uses real use case implementations with fake repositories,
 * following the project's testing pattern.
 */
fun createFakeOverviewUseCases(
    preferencesRepository: FakePreferencesRepository,
    notificationRepository: FakeNotificationRepository,
    entryRepository: EntryRepository = FakeEntryRepository(),
    updateRepository: FakeUpdateRepository = FakeUpdateRepository(),
    currentVersion: String = DEFAULT_CURRENT_VERSION
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
    val setupDailyNotification = SetupDailyNotificationUseCase(notificationRepository)

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

    // Create real update use cases with fake repository
    val checkForUpdate = CheckForUpdateUseCase(
        updateRepository = updateRepository,
        updatePreferencesRepository = preferencesRepository,
        currentVersion = currentVersion
    )
    val dismissUpdate = DismissUpdateUseCase(updatePreferencesRepository = preferencesRepository)
    val downloadUpdate = DownloadUpdateUseCase(
        updateRepository = updateRepository,
        config = DEFAULT_UPDATE_CONFIG
    )
    val getCurrentVersionInfo = GetCurrentVersionInfoUseCase(
        updateRepository = updateRepository,
        currentVersion = currentVersion
    )

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
        checkForUpdate = checkForUpdate,
        dismissUpdate = dismissUpdate,
        downloadUpdate = downloadUpdate,
        getCurrentVersionInfo = getCurrentVersionInfo
    )
}
