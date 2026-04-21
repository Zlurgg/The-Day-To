package uk.co.zlurgg.thedayto.core.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.BuildConfig
import uk.co.zlurgg.thedayto.core.data.database.DatabaseFactory
import uk.co.zlurgg.thedayto.core.data.database.TheDayToDatabase
import uk.co.zlurgg.thedayto.core.data.network.HttpClientFactory
import uk.co.zlurgg.thedayto.core.data.repository.LocalDataClearerImpl
import uk.co.zlurgg.thedayto.core.data.repository.PreferencesRepositoryImpl
import uk.co.zlurgg.thedayto.core.data.util.InAppReviewLauncherImpl
import uk.co.zlurgg.thedayto.core.domain.usecases.theme.GetThemeModeUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.theme.SetThemeModeUseCase
import uk.co.zlurgg.thedayto.core.ui.util.InAppReviewLauncher
import uk.co.zlurgg.thedayto.core.data.util.SystemTimeProvider
import uk.co.zlurgg.thedayto.core.domain.repository.LocalDataClearer
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckTodayEntryExistsUseCase
import uk.co.zlurgg.thedayto.core.domain.util.TimeProvider
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.CheckTodayEntryExistsUseCaseImpl
import uk.co.zlurgg.thedayto.notification.data.scheduler.NotificationSchedulerImpl
import uk.co.zlurgg.thedayto.notification.domain.scheduler.NotificationScheduler

val coreModule = module {

    // Time Provider
    single<TimeProvider> { SystemTimeProvider() }

    // Database
    single { DatabaseFactory(androidContext()) }
    single { get<DatabaseFactory>().create() }
    single { get<TheDayToDatabase>().entryDao }
    single { get<TheDayToDatabase>().moodColorDao }
    single { get<TheDayToDatabase>().pendingSyncDeletionDao }
    single { get<TheDayToDatabase>().notificationSettingsDao }

    // Network
    single { HttpClientFactory.create(enableLogging = BuildConfig.DEBUG) }

    // Preferences
    single { PreferencesRepositoryImpl(androidContext()) }
    single<PreferencesRepository> { get<PreferencesRepositoryImpl>() }

    // In-App Review
    single<InAppReviewLauncher> { InAppReviewLauncherImpl() }

    // Theme Use Cases
    factory { GetThemeModeUseCase(preferencesRepository = get()) }
    factory { SetThemeModeUseCase(preferencesRepository = get()) }

    // Local Data Clearer (for account deletion)
    single<LocalDataClearer> {
        LocalDataClearerImpl(
            entryDao = get(),
            moodColorDao = get(),
            notificationSettingsDao = get(),
            pendingSyncDeletionDao = get(),
            preferencesRepository = get(),
        )
    }

    // CheckTodayEntryExistsUseCase - used by NotificationRepository
    // Note: Depends on EntryRepository from journalModule, resolved at runtime
    single<CheckTodayEntryExistsUseCase> {
        CheckTodayEntryExistsUseCaseImpl(repository = get(), timeProvider = get())
    }

    // Notification Scheduler (WorkManager operations)
    single<NotificationScheduler> {
        NotificationSchedulerImpl(
            context = androidContext(),
            settingsRepository = get(),
            authRepository = get(),
            checkTodayEntryExists = get(),
        )
    }
}
