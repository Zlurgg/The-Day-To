package uk.co.zlurgg.thedayto.journal.di

import org.koin.dsl.module
import uk.co.zlurgg.thedayto.core.data.database.TheDayToDatabase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckNotificationPermissionUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckSystemNotificationsEnabledUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.GetNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.SaveNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.SetupDailyNotificationUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.ShouldShowPermissionRationaleUseCase
import uk.co.zlurgg.thedayto.journal.data.repository.EntryRepositoryImpl
import uk.co.zlurgg.thedayto.journal.data.repository.MoodColorRepositoryImpl
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.AddEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.CheckEditorTutorialSeenUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.EditorUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.MarkEditorTutorialSeenUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolormanagement.MoodColorManagementUseCases
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
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetMoodColorEntryCountsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.AddMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.DeleteMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SeedDefaultMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.UpdateMoodColorNameUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.UpdateMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.CalculateMoodDistributionUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.CalculateMonthlyBreakdownUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.CalculateTotalStatsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.StatsUseCases

val journalModule = module {

    // ========== Repositories ==========

    single<EntryRepository> {
        EntryRepositoryImpl(get<TheDayToDatabase>().entryDao)
    }

    single<MoodColorRepository> {
        MoodColorRepositoryImpl(get<TheDayToDatabase>().moodColorDao)
    }

    // ========== Shared Entry Use Cases ==========

    single { GetEntriesUseCase(repository = get()) }
    single { GetEntriesForMonthUseCase(repository = get()) }
    single { GetEntryByDateUseCase(repository = get()) }
    single { GetEntryUseCase(repository = get()) }
    single { GetMoodColorEntryCountsUseCase(repository = get()) }

    // ========== Shared MoodColor Use Cases ==========

    single { GetMoodColorsUseCase(repository = get()) }
    single { GetMoodColorUseCase(repository = get()) }
    single { AddMoodColorUseCase(repository = get()) }
    single { UpdateMoodColorUseCase(repository = get()) }
    single { UpdateMoodColorNameUseCase(repository = get()) }
    single { DeleteMoodColorUseCase(repository = get()) }
    single { SeedDefaultMoodColorsUseCase(moodColorRepository = get(), preferencesRepository = get()) }

    // ========== Overview Use Cases ==========

    single {
        OverviewUseCases(
            getEntries = get(),
            getEntriesForMonth = get(),
            deleteEntry = DeleteEntryUseCase(repository = get()),
            restoreEntry = RestoreEntryUseCase(repository = get()),
            getEntryByDate = get(),
            updateEntryUseCase = UpdateEntryUseCase(repository = get()),
            checkEntryReminderShownToday = CheckEntryReminderShownTodayUseCase(
                preferencesRepository = get()
            ),
            markEntryReminderShownToday = MarkEntryReminderShownTodayUseCase(
                preferencesRepository = get()
            ),
            checkFirstLaunch = CheckFirstLaunchUseCase(preferencesRepository = get()),
            markFirstLaunchComplete = MarkFirstLaunchCompleteUseCase(preferencesRepository = get()),
            getNotificationSettings = GetNotificationSettingsUseCase(preferencesRepository = get()),
            saveNotificationSettings = SaveNotificationSettingsUseCase(
                preferencesRepository = get(),
                notificationRepository = get()
            ),
            checkNotificationPermission = CheckNotificationPermissionUseCase(
                notificationRepository = get()
            ),
            checkSystemNotificationsEnabled = CheckSystemNotificationsEnabledUseCase(
                notificationRepository = get()
            ),
            shouldShowPermissionRationale = ShouldShowPermissionRationaleUseCase(
                notificationRepository = get()
            ),
            setupDailyNotification = SetupDailyNotificationUseCase(notificationRepository = get()),
            checkForUpdate = get(),
            dismissUpdate = get(),
            downloadUpdate = get(),
            getCurrentVersionInfo = get()
        )
    }

    // ========== Editor Use Cases ==========

    single {
        EditorUseCases(
            getEntryUseCase = get(),
            getEntryByDateUseCase = get(),
            addEntryUseCase = AddEntryUseCase(repository = get(), moodColorRepository = get()),
            getMoodColors = get(),
            deleteMoodColor = get(),
            addMoodColorUseCase = get(),
            getMoodColorUseCase = get(),
            updateMoodColorUseCase = get(),
            updateMoodColorNameUseCase = get(),
            checkEditorTutorialSeen = CheckEditorTutorialSeenUseCase(preferencesRepository = get()),
            markEditorTutorialSeen = MarkEditorTutorialSeenUseCase(preferencesRepository = get())
        )
    }

    // ========== Stats Use Cases ==========

    single {
        StatsUseCases(
            calculateTotalStats = CalculateTotalStatsUseCase(),
            calculateMoodDistribution = CalculateMoodDistributionUseCase(),
            calculateMonthlyBreakdown = CalculateMonthlyBreakdownUseCase()
        )
    }

    // ========== MoodColorManagement Use Cases ==========

    single {
        MoodColorManagementUseCases(
            getMoodColors = get(),
            addMoodColor = get(),
            updateMoodColor = get(),
            updateMoodColorName = get(),
            deleteMoodColor = get(),
            getMoodColorEntryCounts = get()
        )
    }
}
