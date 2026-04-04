package uk.co.zlurgg.thedayto.sync.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.sync.data.repository.SyncRepositoryImpl
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler
import uk.co.zlurgg.thedayto.sync.domain.repository.SyncRepository
import uk.co.zlurgg.thedayto.sync.domain.usecase.GetLastSyncTimestampUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.ObserveSyncStateUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.PerformSyncUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.PrepareForSyncUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.SyncUseCases
import uk.co.zlurgg.thedayto.sync.ui.SyncSettingsViewModel

val syncModule = module {

    // SyncRepository
    // Note: FirebaseFirestore is provided by debugModule (emulator) or releaseModule (production)
    single<SyncRepository> {
        SyncRepositoryImpl(
            firestore = get(),
            entryDao = get(),
            moodColorDao = get(),
            pendingSyncDeletionDao = get(),
            notificationSyncService = get()
        )
    }

    // UseCases
    single {
        PerformSyncUseCase(
            authRepository = get(),
            preferencesRepository = get(),
            syncRepository = get()
        )
    }

    single {
        ObserveSyncStateUseCase(
            syncRepository = get()
        )
    }

    single {
        GetLastSyncTimestampUseCase(
            preferencesRepository = get()
        )
    }

    single {
        PrepareForSyncUseCase(
            syncRepository = get()
        )
    }

    // SyncUseCases aggregator
    single {
        SyncUseCases(
            performSync = get(),
            observeSyncState = get(),
            getLastSyncTimestamp = get(),
            prepareForSync = get()
        )
    }

    // Background sync scheduler
    single { SyncScheduler(androidContext()) }

    // ViewModel
    viewModel {
        SyncSettingsViewModel(
            syncUseCases = get(),
            authRepository = get(),
            syncScheduler = get(),
            signInUseCase = get(),
            signOutUseCase = get(),
            preferencesRepository = get(),
            syncRepository = get(),
            seedDefaultMoodColorsUseCase = get(),
            notificationAuthUseCase = get(),
            devSignInUseCase = getOrNull()
        )
    }
}
