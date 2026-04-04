package uk.co.zlurgg.thedayto.notification.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object for notification settings.
 *
 * Provides CRUD operations for notification settings stored per-user.
 */
@Dao
interface NotificationSettingsDao {

    /**
     * Get settings for a specific user.
     *
     * @param userId "anonymous" or Firebase UID
     * @return Settings entity or null if not configured
     */
    @Query("SELECT * FROM notification_settings WHERE userId = :userId")
    suspend fun getByUserId(userId: String): NotificationSettingsEntity?

    /**
     * Insert or update settings.
     *
     * Uses REPLACE strategy - if userId exists, replaces the row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: NotificationSettingsEntity)

    /**
     * Delete settings for a user.
     *
     * Called on sign-out to clean up user data.
     */
    @Query("DELETE FROM notification_settings WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)

    /**
     * Update sync status for a user.
     *
     * Called after successful sync to mark as SYNCED.
     */
    @Query("UPDATE notification_settings SET syncStatus = :status WHERE userId = :userId")
    suspend fun updateSyncStatus(userId: String, status: String)

    /**
     * Get settings that need to be synced.
     *
     * @param userId User to check for pending sync
     * @return Settings with PENDING_SYNC status, or null
     */
    @Query("SELECT * FROM notification_settings WHERE syncStatus = 'PENDING_SYNC' AND userId = :userId")
    suspend fun getPendingSync(userId: String): NotificationSettingsEntity?

    /**
     * Update last notified date and mark for sync.
     *
     * Called after showing a notification to prevent duplicate notifications.
     * Marks as PENDING_SYNC so the date syncs across devices.
     */
    @Query(
        """
        UPDATE notification_settings
        SET lastNotifiedDateEpoch = :dateEpoch, syncStatus = 'PENDING_SYNC'
        WHERE userId = :userId
        """
    )
    suspend fun updateLastNotifiedDate(userId: String, dateEpoch: Long)
}
