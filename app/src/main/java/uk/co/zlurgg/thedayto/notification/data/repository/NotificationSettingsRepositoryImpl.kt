package uk.co.zlurgg.thedayto.notification.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.error.ErrorMapper
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.notification.data.local.NotificationSettingsDao
import uk.co.zlurgg.thedayto.notification.data.local.NotificationSettingsEntity
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettingsState
import uk.co.zlurgg.thedayto.notification.domain.repository.NotificationSettingsRepository
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus

/**
 * Room-based implementation of [NotificationSettingsRepository].
 *
 * Ensures SharedPreferences data is migrated before any access via lazy migration.
 * Uses [Mutex] with double-checked locking to ensure migration runs exactly once
 * and concurrent callers wait for completion.
 */
class NotificationSettingsRepositoryImpl(
    private val dao: NotificationSettingsDao,
    private val migrationService: NotificationMigrationService,
) : NotificationSettingsRepository {

    private val migrationMutex = Mutex()
    private var migrated = false

    /**
     * Ensures migration is complete before any data access.
     *
     * Thread-safe with double-checked locking:
     * - Fast path: if already migrated, returns immediately
     * - Slow path: acquires mutex, re-checks, migrates if needed
     */
    private suspend fun ensureMigrated() {
        if (migrated) return // Fast path after first migration
        migrationMutex.withLock {
            if (!migrated) {
                migrationService.migrateIfNeeded()
                migrated = true
            }
        }
    }

    override suspend fun getSettingsState(userId: String): Result<NotificationSettingsState, DataError.Local> {
        ensureMigrated()
        return ErrorMapper.safeSuspendCall(TAG) {
            val entity = dao.getByUserId(userId)
            if (entity == null) {
                NotificationSettingsState.NotConfigured
            } else {
                val settings = entity.toDomain()
                if (settings != null) {
                    NotificationSettingsState.Configured(settings)
                } else {
                    // Corrupt data - treat as not configured
                    NotificationSettingsState.NotConfigured
                }
            }
        }
    }

    override suspend fun getSettings(userId: String): Result<NotificationSettings?, DataError.Local> {
        ensureMigrated()
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getByUserId(userId)?.toDomain()
        }
    }

    override suspend fun saveSettings(userId: String, settings: NotificationSettings): EmptyResult<DataError.Local> {
        ensureMigrated()
        return ErrorMapper.safeSuspendCall(TAG) {
            // Check if we have existing settings to preserve syncId
            val existing = dao.getByUserId(userId)
            val entity = if (existing != null) {
                // Update existing - preserve syncId, mark as pending sync
                NotificationSettingsEntity.fromDomain(
                    settings = settings,
                    userId = userId,
                    syncId = existing.syncId,
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = System.currentTimeMillis(),
                )
            } else {
                // New settings
                NotificationSettingsEntity.fromDomain(
                    settings = settings,
                    userId = userId,
                )
            }

            dao.upsert(entity)
        }
    }

    override suspend fun deleteSettings(userId: String): EmptyResult<DataError.Local> {
        ensureMigrated()
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.deleteByUserId(userId)
        }
    }

    companion object {
        private const val TAG = "NotificationSettingsRepository"
    }
}
