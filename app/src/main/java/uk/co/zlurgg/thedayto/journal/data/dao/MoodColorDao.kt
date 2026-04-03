package uk.co.zlurgg.thedayto.journal.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.journal.data.model.MoodColorEntity

@Dao
interface MoodColorDao {
    @Query("SELECT * FROM mood_color WHERE isDeleted = 0")
    fun getMoodColors(): Flow<List<MoodColorEntity>>

    @Query("SELECT * FROM mood_color WHERE id = :id")
    suspend fun getMoodColorById(id: Int): MoodColorEntity?

    @Query("SELECT * FROM mood_color WHERE moodNormalized = :moodNormalized")
    suspend fun getMoodColorByName(moodNormalized: String): MoodColorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodColor(moodColor: MoodColorEntity): Long

    @Query("UPDATE mood_color SET isDeleted = 1 WHERE id = :id")
    suspend fun deleteMoodColor(id: Int)

    @Update
    suspend fun updateMoodColor(moodColor: MoodColorEntity)

    // ==================== Sync Queries ====================

    /**
     * Get all mood colors that need to be synced.
     * Excludes unmodified seeds (updatedAt <= 0) but includes deletes.
     */
    @Query(
        """
        SELECT * FROM mood_color
        WHERE (syncStatus = 'PENDING_SYNC' AND COALESCE(updatedAt, 0) > 0)
           OR syncStatus = 'PENDING_DELETE'
        """
    )
    suspend fun getMoodColorsPendingSync(): List<MoodColorEntity>

    /**
     * Get all mood colors (including deleted) for sync operations.
     */
    @Query("SELECT * FROM mood_color")
    suspend fun getAllMoodColorsForSync(): List<MoodColorEntity>

    /**
     * Get mood color by syncId for conflict resolution.
     */
    @Query("SELECT * FROM mood_color WHERE syncId = :syncId")
    suspend fun getMoodColorBySyncId(syncId: String): MoodColorEntity?

    /**
     * Update sync status for a mood color.
     */
    @Query("UPDATE mood_color SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, status: String)

    /**
     * Update sync fields after successful upload.
     * Only updates if item hasn't been modified since upload started (updatedAt matches).
     * Returns number of rows updated (0 if modified, 1 if updated).
     */
    @Query(
        """
        UPDATE mood_color
        SET syncId = :syncId, userId = :userId, syncStatus = :syncStatus
        WHERE id = :id AND updatedAt = :expectedUpdatedAt
        """
    )
    suspend fun updateSyncFields(
        id: Int,
        syncId: String,
        userId: String,
        syncStatus: String,
        expectedUpdatedAt: Long
    ): Int

    /**
     * Mark all LOCAL_ONLY items as PENDING_SYNC.
     * Called when user signs in to ensure local data gets uploaded.
     */
    @Query("UPDATE mood_color SET syncStatus = 'PENDING_SYNC' WHERE syncStatus = 'LOCAL_ONLY'")
    suspend fun markLocalOnlyAsPendingSync(): Int

    /**
     * Adopt orphaned mood colors (userId = null) by setting userId.
     * Only adopts user-modified items (updatedAt > 0), not default seeds.
     * Called when user signs in to claim local data.
     */
    @Query("UPDATE mood_color SET userId = :userId WHERE userId IS NULL AND COALESCE(updatedAt, 0) > 0")
    suspend fun adoptOrphans(userId: String): Int

    /**
     * Mark all SYNCED mood colors as LOCAL_ONLY.
     * Called on sign-out to prepare data for offline use.
     * Keeps userId intact for user isolation.
     */
    @Query("UPDATE mood_color SET syncStatus = 'LOCAL_ONLY' WHERE syncStatus = 'SYNCED'")
    suspend fun markSyncedAsLocalOnly(): Int

    /**
     * Delete all mood colors belonging to a specific user.
     * Called when a different user signs in to clear previous user's data.
     */
    @Query("DELETE FROM mood_color WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String): Int

    /**
     * Get distinct userIds in the database (excluding null).
     * Used to detect if data from another user exists.
     */
    @Query("SELECT DISTINCT userId FROM mood_color WHERE userId IS NOT NULL")
    suspend fun getDistinctUserIds(): List<String>
}