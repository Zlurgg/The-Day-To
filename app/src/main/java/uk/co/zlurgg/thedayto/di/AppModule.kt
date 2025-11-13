package uk.co.zlurgg.thedayto.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.core.data.database.TheDayToDatabase
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository
import uk.co.zlurgg.thedayto.auth.data.repository.AuthRepositoryImpl
import uk.co.zlurgg.thedayto.auth.data.repository.AuthStateRepositoryImpl
import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository
import uk.co.zlurgg.thedayto.core.data.repository.NotificationRepositoryImpl
import uk.co.zlurgg.thedayto.journal.data.repository.EntryRepositoryImpl
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.core.data.repository.PreferencesRepositoryImpl
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.OverviewUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.DeleteEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.RestoreEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.UpdateEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.CheckEntryReminderShownTodayUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.MarkEntryReminderShownTodayUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntriesUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryByDateUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.AddEntryUseCase
import uk.co.zlurgg.thedayto.journal.data.repository.MoodColorRepositoryImpl
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.AddMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.DeleteMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.UpdateMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SeedDefaultMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.EditorUseCases
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckNotificationPermissionUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckSystemNotificationsEnabledUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckTodayEntryExistsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.ShouldShowPermissionRationaleUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.GetNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.SaveNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.CheckTodayEntryExistsUseCaseImpl
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckTodayEntryUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCases
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignOutUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckSignInStatusUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckWelcomeDialogSeenUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.MarkWelcomeDialogSeenUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.CheckFirstLaunchUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.MarkFirstLaunchCompleteUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.CalculateMoodDistributionUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.CalculateMonthlyBreakdownUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.CalculateTotalStatsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.StatsUseCases

val appModule = module {

    single {
        Room.databaseBuilder(
            androidApplication(),
            TheDayToDatabase::class.java,
            TheDayToDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.execSQL("PRAGMA foreign_keys=ON")
                }
            })
            .build()
    }

    // Auth Repository (wraps GoogleAuthUiClient)
    single<AuthRepository> {
        AuthRepositoryImpl(
            context = androidContext()
        )
    }

    // Auth State Repository
    single<AuthStateRepository> {
        AuthStateRepositoryImpl(androidContext())
    }

    // Notification Repository
    single<NotificationRepository> {
        NotificationRepositoryImpl(
            context = androidContext(),
            preferencesRepository = get(),
            checkTodayEntryExists = get()
        )
    }

    // Journal-specific repositories
    single<EntryRepository> { EntryRepositoryImpl(get<TheDayToDatabase>().entryDao) }

    single<PreferencesRepository> {
        PreferencesRepositoryImpl(androidContext())
    }

    // Shared Entry Use Cases (used by multiple features)
    single { GetEntriesUseCase(repository = get()) }
    single { GetEntryByDateUseCase(repository = get()) }
    single { GetEntryUseCase(repository = get()) }

    single {
        OverviewUseCases(
            getEntries = get(),
            deleteEntry = DeleteEntryUseCase(repository = get()),
            restoreEntry = RestoreEntryUseCase(repository = get()),
            getEntryByDate = get(),
            updateEntryUseCase = UpdateEntryUseCase(repository = get()),
            checkEntryReminderShownToday = CheckEntryReminderShownTodayUseCase(preferencesRepository = get()),
            markEntryReminderShownToday = MarkEntryReminderShownTodayUseCase(preferencesRepository = get()),
            checkFirstLaunch = CheckFirstLaunchUseCase(preferencesRepository = get()),
            markFirstLaunchComplete = MarkFirstLaunchCompleteUseCase(preferencesRepository = get()),
            getNotificationSettings = GetNotificationSettingsUseCase(preferencesRepository = get()),
            saveNotificationSettings = SaveNotificationSettingsUseCase(
                preferencesRepository = get(),
                notificationRepository = get()
            ),
            checkNotificationPermission = CheckNotificationPermissionUseCase(notificationRepository = get()),
            checkSystemNotificationsEnabled = CheckSystemNotificationsEnabledUseCase(notificationRepository = get()),
            shouldShowPermissionRationale = ShouldShowPermissionRationaleUseCase(notificationRepository = get())
        )
    }

    single<MoodColorRepository> { MoodColorRepositoryImpl(get<TheDayToDatabase>().moodColorDao) }

    // Shared MoodColor Use Cases (used by multiple features)
    single { GetMoodColorsUseCase(repository = get()) }
    single { GetMoodColorUseCase(repository = get()) }
    single { AddMoodColorUseCase(repository = get()) }
    single { UpdateMoodColorUseCase(repository = get()) }
    single { DeleteMoodColorUseCase(repository = get()) }
    single { SeedDefaultMoodColorsUseCase(moodColorRepository = get(), preferencesRepository = get()) }

    single {
        EditorUseCases(
            getEntryUseCase = get(),
            addEntryUseCase = AddEntryUseCase(repository = get(), moodColorRepository = get()),
            getMoodColors = get(),
            deleteMoodColor = get(),
            addMoodColorUseCase = get(),
            getMoodColorUseCase = get(),
            updateMoodColorUseCase = get()
        )
    }

    // Auth UseCases
    single {
        SignInUseCases(
            signIn = SignInUseCase(
                authRepository = get(),
                authStateRepository = get()
            ),
            checkSignInStatus = CheckSignInStatusUseCase(
                authRepository = get(),
                authStateRepository = get()
            ),
            checkTodayEntry = CheckTodayEntryUseCase(entryRepository = get()),
            seedDefaultMoodColors = get(),
            checkWelcomeDialogSeen = CheckWelcomeDialogSeenUseCase(
                preferencesRepository = get()
            ),
            markWelcomeDialogSeen = MarkWelcomeDialogSeenUseCase(
                preferencesRepository = get()
            )
        )
    }

    // Standalone SignOutUseCase - injected separately into OverviewViewModel
    single {
        SignOutUseCase(
            authRepository = get(),
            authStateRepository = get()
        )
    }

    // CheckTodayEntryExistsUseCase - bind interface to implementation
    // Used by NotificationWorker to check if notification should be sent
    single<CheckTodayEntryExistsUseCase> {
        CheckTodayEntryExistsUseCaseImpl(repository = get())
    }

    // Stats UseCases
    single {
        StatsUseCases(
            calculateTotalStats = CalculateTotalStatsUseCase(),
            calculateMoodDistribution = CalculateMoodDistributionUseCase(),
            calculateMonthlyBreakdown = CalculateMonthlyBreakdownUseCase()
        )
    }

}