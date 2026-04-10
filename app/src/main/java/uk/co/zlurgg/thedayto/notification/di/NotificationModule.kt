package uk.co.zlurgg.thedayto.notification.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService
import uk.co.zlurgg.thedayto.notification.data.repository.NotificationSettingsRepositoryImpl
import uk.co.zlurgg.thedayto.notification.data.sync.NotificationSyncServiceImpl
import uk.co.zlurgg.thedayto.notification.domain.repository.NotificationSettingsRepository
import uk.co.zlurgg.thedayto.notification.domain.sync.NotificationSyncService
import uk.co.zlurgg.thedayto.notification.domain.usecase.NotificationAuthUseCase

private const val PREFS_NAME = "journal_prefs"

val notificationModule = module {

    // SharedPreferences for migration (same as PreferencesRepository uses)
    single(qualifier = org.koin.core.qualifier.named("legacyNotificationPrefs")) {
        androidContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Migration service
    single {
        NotificationMigrationService(
            dao = get(),
            legacyPrefs = get(qualifier = org.koin.core.qualifier.named("legacyNotificationPrefs")),
            authRepository = get(),
        )
    }

    // Repository
    single<NotificationSettingsRepository> {
        NotificationSettingsRepositoryImpl(
            dao = get(),
            migrationService = get(),
        )
    }

    // Sync service
    single<NotificationSyncService> {
        NotificationSyncServiceImpl(
            firestore = get(),
            settingsDao = get(),
        )
    }

    // Use cases
    single {
        NotificationAuthUseCase(
            settingsRepository = get(),
            notificationScheduler = get(),
            syncService = get(),
        )
    }
}
