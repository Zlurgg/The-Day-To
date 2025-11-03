package uk.co.zlurgg.thedayto.di

import androidx.room.Room
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
import uk.co.zlurgg.thedayto.journal.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.journal.data.repository.PreferencesRepositoryImpl
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.OverviewUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.DeleteEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.GetEntriesUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.RestoreEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.GetEntryByDateUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.UpdateEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.CheckEntryReminderShownTodayUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.MarkEntryReminderShownTodayUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.AddEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.GetEntryUseCase
import uk.co.zlurgg.thedayto.journal.data.repository.MoodColorRepositoryImpl
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.AddMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.DeleteMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.GetMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.GetMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.EditorUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.UpdateMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.SetupNotificationUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.CheckNotificationPermissionUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckTodayEntryUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCases
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignOutUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckSignInStatusUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckWelcomeDialogSeenUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.MarkWelcomeDialogSeenUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.auth.SeedDefaultMoodColorsUseCase

val appModule = module {

    single {
        Room.databaseBuilder(
            androidApplication(),
            TheDayToDatabase::class.java,
            TheDayToDatabase.DATABASE_NAME
        ).build()
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
            preferencesRepository = get()
        )
    }

    // Journal-specific repositories
    single<EntryRepository> { EntryRepositoryImpl(get<TheDayToDatabase>().entryDao) }

    single<PreferencesRepository> {
        PreferencesRepositoryImpl(androidContext())
    }

    single {
        OverviewUseCases(
            getEntries = GetEntriesUseCase(repository = get()),
            deleteEntry = DeleteEntryUseCase(repository = get()),
            restoreEntry = RestoreEntryUseCase(repository = get()),
            getEntryByDate = GetEntryByDateUseCase(repository = get()),
            updateEntryUseCase = UpdateEntryUseCase(repository = get()),
            setupNotification = SetupNotificationUseCase(notificationRepository = get()),
            checkNotificationPermission = CheckNotificationPermissionUseCase(notificationRepository = get()),
            checkEntryReminderShownToday = CheckEntryReminderShownTodayUseCase(preferencesRepository = get()),
            markEntryReminderShownToday = MarkEntryReminderShownTodayUseCase(preferencesRepository = get())
        )
    }

    single<MoodColorRepository> { MoodColorRepositoryImpl(get<TheDayToDatabase>().moodColorDao) }

    single {
        EditorUseCases(
            getEntryUseCase = GetEntryUseCase(repository = get()),
            addEntryUseCase = AddEntryUseCase(repository = get()),
            getMoodColors = GetMoodColorsUseCase(repository = get()),
            deleteMoodColor = DeleteMoodColorUseCase(repository = get()),
            addMoodColorUseCase = AddMoodColorUseCase(repository = get()),
            getMoodColorUseCase = GetMoodColorUseCase(repository = get()),
            updateMoodColorUseCase = UpdateMoodColorUseCase(repository = get())
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
            seedDefaultMoodColors = SeedDefaultMoodColorsUseCase(
                moodColorRepository = get(),
                preferencesRepository = get()
            ),
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

}