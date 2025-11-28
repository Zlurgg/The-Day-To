package uk.co.zlurgg.thedayto.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uk.co.zlurgg.thedayto.core.data.database.TheDayToDatabase
import uk.co.zlurgg.thedayto.update.data.remote.api.GitHubApiService
import java.util.concurrent.TimeUnit
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
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntriesForMonthUseCase
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
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.CheckEditorTutorialSeenUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.EditorUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.MarkEditorTutorialSeenUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckNotificationPermissionUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckSystemNotificationsEnabledUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckTodayEntryExistsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.GetNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.SaveNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.SetupDailyNotificationUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.ShouldShowPermissionRationaleUseCase
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
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetMoodColorEntryCountsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolormanagement.MoodColorManagementUseCases
import uk.co.zlurgg.thedayto.update.data.repository.UpdateRepositoryImpl
import uk.co.zlurgg.thedayto.update.data.service.ApkDownloadService
import uk.co.zlurgg.thedayto.update.domain.repository.UpdateRepository
import uk.co.zlurgg.thedayto.update.domain.usecases.CheckForUpdateUseCase
import uk.co.zlurgg.thedayto.update.domain.usecases.DismissUpdateUseCase
import uk.co.zlurgg.thedayto.update.domain.usecases.DownloadUpdateUseCase

private const val GITHUB_API_BASE_URL = "https://api.github.com/"
private const val NETWORK_TIMEOUT_SECONDS = 30L
private const val CURRENT_VERSION = uk.co.zlurgg.thedayto.BuildConfig.VERSION_NAME

val appModule = module {

    // Database
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

    // Network - OkHttpClient
    single {
        OkHttpClient.Builder()
            .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    // Network - Retrofit
    single {
        Retrofit.Builder()
            .baseUrl(GITHUB_API_BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // GitHub API Service
    single<GitHubApiService> {
        get<Retrofit>().create(GitHubApiService::class.java)
    }

    // Update Feature - APK Download Service
    single { ApkDownloadService(androidContext()) }

    // Update Feature - Repository
    single<UpdateRepository> {
        UpdateRepositoryImpl(
            gitHubApiService = get(),
            apkDownloadService = get()
        )
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
    single { GetEntriesForMonthUseCase(repository = get()) }
    single { GetEntryByDateUseCase(repository = get()) }
    single { GetEntryUseCase(repository = get()) }

    single {
        OverviewUseCases(
            getEntries = get(),
            getEntriesForMonth = get(),
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
            shouldShowPermissionRationale = ShouldShowPermissionRationaleUseCase(notificationRepository = get()),
            setupDailyNotification = SetupDailyNotificationUseCase(notificationRepository = get()),
            checkForUpdate = CheckForUpdateUseCase(
                updateRepository = get(),
                preferencesRepository = get(),
                currentVersion = CURRENT_VERSION
            ),
            dismissUpdate = DismissUpdateUseCase(preferencesRepository = get()),
            downloadUpdate = DownloadUpdateUseCase(updateRepository = get())
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

    // Entry count use case (used by MoodColorManagement)
    single { GetMoodColorEntryCountsUseCase(repository = get()) }

    // MoodColorManagement UseCases aggregator
    single {
        MoodColorManagementUseCases(
            getMoodColors = get(),
            addMoodColor = get(),
            updateMoodColor = get(),
            deleteMoodColor = get(),
            getMoodColorEntryCounts = get()
        )
    }

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
            checkEditorTutorialSeen = CheckEditorTutorialSeenUseCase(preferencesRepository = get()),
            markEditorTutorialSeen = MarkEditorTutorialSeenUseCase(preferencesRepository = get())
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