package uk.co.zlurgg.thedayto.notification.data.migration

import android.content.SharedPreferences
import androidx.core.content.edit
import timber.log.Timber
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.notification.data.local.NotificationSettingsDao
import uk.co.zlurgg.thedayto.notification.data.local.NotificationSettingsEntity
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus
import java.util.UUID

/**
 * One-time migration from SharedPreferences to Room for notification settings.
 *
 * Migration is idempotent:
 * - Checks if legacy keys exist in SharedPreferences
 * - If legacy data exists and Room doesn't have data for the user, migrates it
 * - Cleans up SharedPreferences after successful migration
 *
 * This is called lazily by [NotificationSettingsRepositoryImpl] on first access,
 * ensuring migration happens before any reads.
 */
class NotificationMigrationService(
    private val dao: NotificationSettingsDao,
    private val legacyPrefs: SharedPreferences,
    private val authRepository: AuthRepository,
) {
    /**
     * Migrates notification settings from SharedPreferences to Room if needed.
     *
     * Safe to call multiple times - only migrates once per user.
     */
    suspend fun migrateIfNeeded() {
        // Check if legacy keys exist (source of truth for "needs migration")
        val hasLegacyData = legacyPrefs.contains(KEY_NOTIFICATION_ENABLED)
        if (!hasLegacyData) {
            Timber.d("No legacy notification data to migrate")
            return
        }

        val userId = authRepository.getSignedInUser()?.userId ?: ANONYMOUS_USER_ID

        // Check if already migrated to Room
        val existingSettings = dao.getByUserId(userId)
        if (existingSettings != null) {
            // Already in Room - just clean up SharedPreferences
            Timber.d("Settings already exist in Room for user %s, cleaning up legacy", userId)
            cleanupLegacyPrefs()
            return
        }

        // Read legacy values
        val enabled = legacyPrefs.getBoolean(KEY_NOTIFICATION_ENABLED, false)
        val hour = legacyPrefs.getInt(KEY_NOTIFICATION_HOUR, DEFAULT_HOUR)
        val minute = legacyPrefs.getInt(KEY_NOTIFICATION_MINUTE, DEFAULT_MINUTE)

        Timber.d(
            "Migrating notification settings: enabled=%b, hour=%d, minute=%d, userId=%s",
            enabled,
            hour,
            minute,
            userId,
        )

        // Insert into Room
        dao.upsert(
            NotificationSettingsEntity(
                userId = userId,
                enabled = enabled,
                hour = hour,
                minute = minute,
                syncId = UUID.randomUUID().toString(),
                syncStatus = SyncStatus.PENDING_SYNC.name,
                updatedAt = System.currentTimeMillis(),
            ),
        )

        // Clean up SharedPreferences (safe to crash here - Room is source of truth)
        cleanupLegacyPrefs()
        Timber.d("Successfully migrated notification settings to Room")
    }

    private fun cleanupLegacyPrefs() {
        legacyPrefs.edit {
            remove(KEY_NOTIFICATION_ENABLED)
            remove(KEY_NOTIFICATION_HOUR)
            remove(KEY_NOTIFICATION_MINUTE)
        }
    }

    companion object {
        const val ANONYMOUS_USER_ID = "anonymous"

        // Legacy SharedPreferences keys (from PreferencesRepositoryImpl)
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_NOTIFICATION_HOUR = "notification_hour"
        private const val KEY_NOTIFICATION_MINUTE = "notification_minute"
        private const val DEFAULT_HOUR = 9
        private const val DEFAULT_MINUTE = 0
    }
}
