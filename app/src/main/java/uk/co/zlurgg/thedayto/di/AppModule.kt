package uk.co.zlurgg.thedayto.di

import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.core.data.database.TheDayToDatabase
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository
import uk.co.zlurgg.thedayto.auth.data.repository.AuthStateRepositoryImpl
import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository
import uk.co.zlurgg.thedayto.core.data.repository.NotificationRepositoryImpl
import uk.co.zlurgg.thedayto.auth.data.service.GoogleAuthUiClient
import uk.co.zlurgg.thedayto.journal.data.repository.EntryRepositoryImpl
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.entry.AddEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.entry.EntryUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.entry.DeleteEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.entry.GetEntriesUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.entry.GetEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.entry.GetEntryByDateUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.entry.UpdateEntryUseCase
import uk.co.zlurgg.thedayto.journal.data.repository.MoodColorRepositoryImpl
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolor.AddMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolor.DeleteMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolor.GetMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolor.GetMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolor.MoodColorUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolor.UpdateMoodColorUseCase

val appModule = module {

    single {
        Room.databaseBuilder(
            androidApplication(),
            TheDayToDatabase::class.java,
            TheDayToDatabase.DATABASE_NAME
        ).build()
    }

    // Google Sign-In Client (Modern Credential Manager API)
    single {
        GoogleAuthUiClient(
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
            getEntryByDateUseCase = GetEntryByDateUseCase(repository = get())
        )
    }

//    viewModelOf(::AddEditEntryViewModel)


    single<EntryRepository> { EntryRepositoryImpl(get<TheDayToDatabase>().entryDao) }

    single {
        EntryUseCases(
            getEntries = GetEntriesUseCase(repository = get()),
            deleteEntry = DeleteEntryUseCase(repository = get()),
            addEntryUseCase = AddEntryUseCase(repository = get()),
            getEntryUseCase = GetEntryUseCase(repository = get()),
            getEntryByDate = GetEntryByDateUseCase(repository = get()),
            updateEntryUseCase = UpdateEntryUseCase(repository = get())
        )
    }

    single<MoodColorRepository> { MoodColorRepositoryImpl(get<TheDayToDatabase>().moodColorDao) }

    single {
        MoodColorUseCases(
            getMoodColors = GetMoodColorsUseCase(repository = get()),
            deleteMoodColor = DeleteMoodColorUseCase(repository = get()),
            addMoodColorUseCase = AddMoodColorUseCase(repository = get()),
            getMoodColorUseCase = GetMoodColorUseCase(repository = get()),
            updateMoodColorUseCase = UpdateMoodColorUseCase(repository = get())
        )
    }

}