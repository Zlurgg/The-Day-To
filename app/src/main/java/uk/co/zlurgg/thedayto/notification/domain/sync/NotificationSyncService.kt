package uk.co.zlurgg.thedayto.notification.domain.sync

import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings

/**
 * Service for syncing notification settings with Firestore.
 *
 * Follows the same patterns as entry/mood color sync:
 * - Upload pending local changes
 * - Download remote settings
 * - Conflict resolution (last-write-wins)
 */
interface NotificationSyncService {

    /**
     * Upload pending notification settings to Firestore.
     *
     * Uploads settings with PENDING_SYNC status and marks them as SYNCED on success.
     *
     * @param userId Firebase UID of the user
     * @return Result with count of uploaded settings (0 or 1)
     */
    suspend fun uploadPending(userId: String): Result<Int, DataError.Sync>

    /**
     * Download notification settings from Firestore.
     *
     * Downloads remote settings and applies conflict resolution if local settings exist.
     * Uses last-write-wins based on updatedAt timestamp.
     *
     * @param userId Firebase UID of the user
     * @return Result with downloaded settings or null if none exist remotely
     */
    suspend fun download(userId: String): Result<NotificationSettings?, DataError.Sync>

    /**
     * Delete notification settings from Firestore.
     *
     * Called during pending deletion processing.
     *
     * @param syncId The sync ID of the settings to delete
     * @param userId Firebase UID of the user
     * @return EmptyResult indicating success or failure
     */
    suspend fun deleteRemote(syncId: String, userId: String): EmptyResult<DataError.Sync>
}
