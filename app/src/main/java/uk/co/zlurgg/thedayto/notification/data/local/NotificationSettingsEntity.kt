package uk.co.zlurgg.thedayto.notification.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import timber.log.Timber
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus
import java.util.UUID

/**
 * Room entity for notification settings.
 *
 * Uses userId as primary key to support multiple users:
 * - "anonymous" for signed-out users
 * - Firebase UID for signed-in users
 *
 * No init validation - corrupt DB data shouldn't crash the app.
 * Use [toDomain] which returns null for invalid data.
 */
@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey val userId: String,
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
    val syncId: String,
    val syncStatus: String,
    val updatedAt: Long,
    val lastNotifiedDateEpoch: Long = 0,
) {
    /**
     * Converts to domain model. Returns null if data is corrupt.
     *
     * Corrupt data is logged but doesn't crash - user can reconfigure.
     * This is preferable to crashing on app startup with corrupt DB data.
     */
    fun toDomain(): NotificationSettings? {
        if (hour !in 0..NotificationSettings.MAX_HOUR || minute !in 0..NotificationSettings.MAX_MINUTE) {
            Timber.e("Invalid notification settings: hour=%d, minute=%d, userId=%s", hour, minute, userId)
            return null
        }
        return NotificationSettings(
            enabled = enabled,
            hour = hour,
            minute = minute,
        )
    }

    companion object {
        /**
         * Creates an entity from a domain model.
         *
         * @param settings Domain model with validated hour/minute
         * @param userId "anonymous" or Firebase UID
         * @param syncId Unique sync identifier, defaults to new UUID
         * @param syncStatus Sync state, defaults to PENDING_SYNC for new saves
         * @param updatedAt Timestamp, defaults to current time
         */
        fun fromDomain(
            settings: NotificationSettings,
            userId: String,
            syncId: String = UUID.randomUUID().toString(),
            syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
            updatedAt: Long = System.currentTimeMillis(),
            lastNotifiedDateEpoch: Long = 0,
        ) = NotificationSettingsEntity(
            userId = userId,
            enabled = settings.enabled,
            hour = settings.hour,
            minute = settings.minute,
            syncId = syncId,
            syncStatus = syncStatus.name,
            updatedAt = updatedAt,
            lastNotifiedDateEpoch = lastNotifiedDateEpoch,
        )
    }
}
