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
     * Get all mood colors that need to be synced (PENDING_SYNC or PENDING_DELETE).
     */
    @Query("SELECT * FROM mood_color WHERE syncStatus IN ('PENDING_SYNC', 'PENDING_DELETE')")
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
     */
    @Query(
        """
        UPDATE mood_color
        SET syncId = :syncId, userId = :userId, updatedAt = :updatedAt, syncStatus = :syncStatus
        WHERE id = :id
        """
    )
    suspend fun updateSyncFields(id: Int, syncId: String, userId: String, updatedAt: Long, syncStatus: String)

    /**
     * Mark all LOCAL_ONLY items as PENDING_SYNC.
     * Called when user signs in to ensure local data gets uploaded.
     */
    @Query("UPDATE mood_color SET syncStatus = 'PENDING_SYNC' WHERE syncStatus = 'LOCAL_ONLY'")
    suspend fun markLocalOnlyAsPendingSync(): Int
}