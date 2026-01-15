package uk.co.zlurgg.thedayto.core.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.BuildConfig
import uk.co.zlurgg.thedayto.core.data.database.DatabaseFactory
import uk.co.zlurgg.thedayto.core.data.database.TheDayToDatabase
import uk.co.zlurgg.thedayto.core.data.network.HttpClientFactory
import uk.co.zlurgg.thedayto.core.data.repository.NotificationRepositoryImpl
import uk.co.zlurgg.thedayto.core.data.repository.PreferencesRepositoryImpl
import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckTodayEntryExistsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.CheckTodayEntryExistsUseCaseImpl
import io.github.zlurgg.update.domain.model.UpdateConfig
import io.github.zlurgg.update.domain.repository.UpdatePreferencesRepository

val coreModule = module {

    // Update module configuration
    single {
        UpdateConfig(
            gitHubOwner = "Zlurgg",
            gitHubRepo = "The-Day-To",
            appName = "the-day-to"
        )
    }
    single(named("currentVersion")) { BuildConfig.VERSION_NAME }

    // Database
    single { DatabaseFactory(androidContext()) }
    single { get<DatabaseFactory>().create() }
    single { get<TheDayToDatabase>().entryDao }
    single { get<TheDayToDatabase>().moodColorDao }

    // Network
    single { HttpClientFactory.create(enableLogging = BuildConfig.DEBUG) }

    // Preferences
    single { PreferencesRepositoryImpl(androidContext()) }
    single<PreferencesRepository> { get<PreferencesRepositoryImpl>() }
    single<UpdatePreferencesRepository> { get<PreferencesRepositoryImpl>() }

    // CheckTodayEntryExistsUseCase - used by NotificationRepository
    // Note: Depends on EntryRepository from journalModule, resolved at runtime
    single<CheckTodayEntryExistsUseCase> {
        CheckTodayEntryExistsUseCaseImpl(repository = get())
    }

    // Notification Repository
    single<NotificationRepository> {
        NotificationRepositoryImpl(
            context = androidContext(),
            preferencesRepository = get(),
            checkTodayEntryExists = get()
        )
    }
}
