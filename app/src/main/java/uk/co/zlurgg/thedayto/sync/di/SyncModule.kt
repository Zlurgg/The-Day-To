package uk.co.zlurgg.thedayto.sync.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.auth.domain.usecases.AccountUseCases
import uk.co.zlurgg.thedayto.auth.domain.usecases.GetSignedInUserUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.ReauthenticateUseCase
import uk.co.zlurgg.thedayto.sync.data.repository.SyncRepositoryImpl
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler
import uk.co.zlurgg.thedayto.sync.domain.repository.SyncRepository
import uk.co.zlurgg.thedayto.sync.domain.usecase.GetLastSyncTimestampUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.ObserveSyncStateUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.PerformSyncUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.PrepareForSyncUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.SetSyncEnabledUseCase
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
            notificationSyncService = get(),
        )
    }

    // UseCases
    single {
        PerformSyncUseCase(
            authRepository = get(),
            preferencesRepository = get(),
            syncRepository = get(),
        )
    }

    single {
        ObserveSyncStateUseCase(
            syncRepository = get(),
        )
    }

    single {
        GetLastSyncTimestampUseCase(
            preferencesRepository = get(),
        )
    }

    single {
        PrepareForSyncUseCase(
            syncRepository = get(),
        )
    }

    single {
        SetSyncEnabledUseCase(
            preferencesRepository = get(),
        )
    }

    // SyncUseCases aggregator
    single {
        SyncUseCases(
            performSync = get(),
            observeSyncState = get(),
            getLastSyncTimestamp = get(),
            prepareForSync = get(),
            setSyncEnabled = get(),
        )
    }

    // Auth UseCases for account management
    single {
        GetSignedInUserUseCase(
            authRepository = get(),
        )
    }

    single {
        ReauthenticateUseCase(
            authRepository = get(),
        )
    }

    // AccountUseCases aggregator
    single {
        AccountUseCases(
            getSignedInUser = get(),
            signIn = get(),
            signOut = get(),
            reauthenticate = get(),
            deleteAccount = get(),
            devSignIn = getOrNull(),
        )
    }

    // Background sync scheduler
    single { SyncScheduler(androidContext()) }

    // ViewModel
    viewModel {
        SyncSettingsViewModel(
            syncUseCases = get(),
            accountUseCases = get(),
            syncScheduler = get(),
            notificationAuthUseCase = get(),
        )
    }
}
