package uk.co.zlurgg.thedayto.sync.di

import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.sync.data.repository.SyncRepositoryImpl
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler
import uk.co.zlurgg.thedayto.sync.domain.repository.SyncRepository
import uk.co.zlurgg.thedayto.sync.domain.usecase.CheckSyncEnabledUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.DisableSyncUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.EnableSyncUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.GetLastSyncTimestampUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.ObserveSyncStateUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.PerformSyncUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.SyncUseCases
import uk.co.zlurgg.thedayto.sync.ui.SyncSettingsViewModel

val syncModule = module {

    // Firebase Firestore instance
    single { FirebaseFirestore.getInstance() }

    // SyncRepository
    single<SyncRepository> {
        SyncRepositoryImpl(
            firestore = get(),
            entryDao = get(),
            moodColorDao = get()
        )
    }

    // UseCases
    single {
        EnableSyncUseCase(
            authRepository = get(),
            preferencesRepository = get(),
            syncRepository = get()
        )
    }

    single {
        DisableSyncUseCase(
            preferencesRepository = get()
        )
    }

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
        CheckSyncEnabledUseCase(
            preferencesRepository = get()
        )
    }

    single {
        GetLastSyncTimestampUseCase(
            preferencesRepository = get()
        )
    }

    // SyncUseCases aggregator
    single {
        SyncUseCases(
            enableSync = get(),
            disableSync = get(),
            performSync = get(),
            observeSyncState = get(),
            checkSyncEnabled = get(),
            getLastSyncTimestamp = get()
        )
    }

    // Background sync scheduler
    single { SyncScheduler(androidContext()) }

    // ViewModel
    viewModel {
        SyncSettingsViewModel(
            syncUseCases = get(),
            authRepository = get(),
            syncScheduler = get()
        )
    }
}
