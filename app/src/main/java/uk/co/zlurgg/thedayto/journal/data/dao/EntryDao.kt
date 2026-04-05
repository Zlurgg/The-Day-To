package uk.co.zlurgg.thedayto.journal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.journal.data.model.EntryEntity
import uk.co.zlurgg.thedayto.journal.data.model.EntryWithMoodColorEntity

@Dao
interface EntryDao {

    @Query("SELECT * FROM entry")
    fun getEntries(): Flow<List<EntryEntity>>

    @Query("""
        SELECT e.id, e.moodColorId, e.content, e.dateStamp,
               mc.mood as moodName, mc.color as moodColor
        FROM entry e
        INNER JOIN mood_color mc ON e.moodColorId = mc.id
        ORDER BY e.dateStamp DESC
    """)
    fun getEntriesWithMoodColors(): Flow<List<EntryWithMoodColorEntity>>

    /**
     * Get entries within a specific date range (for month filtering).
     *
     * Filters entries at the database level using a WHERE clause for optimal performance.
     * This approach scales efficiently as users accumulate entries over time.
     *
     * @param startEpoch Start of the range (inclusive) - epoch seconds
     * @param endEpoch End of the range (exclusive) - epoch seconds
     * @return Flow of entries with mood colors within the date range, ordered by date descending
     */
    @Query("""
        SELECT e.id, e.moodColorId, e.content, e.dateStamp,
               mc.mood as moodName, mc.color as moodColor
        FROM entry e
        INNER JOIN mood_color mc ON e.moodColorId = mc.id
        WHERE e.dateStamp >= :startEpoch AND e.dateStamp < :endEpoch
        ORDER BY e.dateStamp DESC
    """)
    fun getEntriesForMonth(startEpoch: Long, endEpoch: Long): Flow<List<EntryWithMoodColorEntity>>

    @Query("SELECT * FROM entry WHERE id = :id")
    suspend fun getEntryById(id: Int): EntryEntity?

    @Query("""
        SELECT e.id, e.moodColorId, e.content, e.dateStamp,
               mc.mood as moodName, mc.color as moodColor
        FROM entry e
        INNER JOIN mood_color mc ON e.moodColorId = mc.id
        WHERE e.id = :id
    """)
    suspend fun getEntryWithMoodColorById(id: Int): EntryWithMoodColorEntity?

    @Query("SELECT * FROM entry WHERE dateStamp = :date")
    suspend fun getEntryByDate(date: Long): EntryEntity?

    @Query("""
        SELECT e.id, e.moodColorId, e.content, e.dateStamp,
               mc.mood as moodName, mc.color as moodColor
        FROM entry e
        INNER JOIN mood_color mc ON e.moodColorId = mc.id
        WHERE e.dateStamp = :date
    """)
    suspend fun getEntryWithMoodColorByDate(date: Long): EntryWithMoodColorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: EntryEntity)

    /**
     * Hard delete an entry from the database by ID.
     */
    @Query("DELETE FROM entry WHERE id = :id")
    suspend fun deleteEntry(id: Int)

    /**
     * Hard delete an entry from the database.
     * Used by sync after successful remote deletion.
     */
    @Delete
    suspend fun hardDeleteEntry(entry: EntryEntity)

    @Update
    suspend fun updateEntry(entry: EntryEntity)

    /**
     * Get the count of entries for each mood color.
     * Used by the Mood Color Management screen to show usage statistics.
     *
     * @return Flow of pairs (moodColorId to entryCount)
     */
    @Query("""
        SELECT moodColorId, COUNT(*) as entryCount
        FROM entry
        GROUP BY moodColorId
    """)
    fun getMoodColorEntryCounts(): Flow<List<MoodColorEntryCount>>

    // ==================== Sync Queries ====================

    /**
     * Get all entries that need to be synced.
     * Note: PENDING_DELETE entries are tracked separately in pending_sync_deletion table.
     */
    @Query("SELECT * FROM entry WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getEntriesPendingSync(): List<EntryEntity>

    /**
     * Get entry by syncId for conflict resolution.
     */
    @Query("SELECT * FROM entry WHERE syncId = :syncId")
    suspend fun getEntryBySyncId(syncId: String): EntryEntity?

    /**
     * Update sync status for an entry.
     */
    @Query("UPDATE entry SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, status: String)

    /**
     * Update sync fields after successful upload.
     * Only updates if entry hasn't been modified since upload started (updatedAt matches).
     * Returns number of rows updated (0 if entry was modified, 1 if updated).
     */
    @Query("""
        UPDATE entry
        SET syncId = :syncId, userId = :userId, syncStatus = :syncStatus
        WHERE id = :id AND updatedAt = :expectedUpdatedAt
    """)
    suspend fun updateSyncFields(
        id: Int,
        syncId: String,
        userId: String,
        syncStatus: String,
        expectedUpdatedAt: Long
    ): Int

    /**
     * Mark all LOCAL_ONLY entries as PENDING_SYNC.
     * Called when user signs in to ensure local data gets uploaded.
     */
    @Query("UPDATE entry SET syncStatus = 'PENDING_SYNC' WHERE syncStatus = 'LOCAL_ONLY'")
    suspend fun markLocalOnlyAsPendingSync(): Int

    /**
     * Adopt orphaned entries (userId = null) by setting userId.
     * Called when user signs in to claim local data.
     */
    @Query("UPDATE entry SET userId = :userId WHERE userId IS NULL")
    suspend fun adoptOrphans(userId: String): Int

    /**
     * Mark all SYNCED entries as LOCAL_ONLY.
     * Called on sign-out to prepare data for offline use.
     * Keeps userId intact for user isolation.
     */
    @Query("UPDATE entry SET syncStatus = 'LOCAL_ONLY' WHERE syncStatus = 'SYNCED'")
    suspend fun markSyncedAsLocalOnly(): Int

    /**
     * Delete all entries belonging to a specific user.
     * Called when a different user signs in to clear previous user's data.
     */
    @Query("DELETE FROM entry WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String): Int

    /**
     * Get distinct userIds in the database (excluding null).
     * Used to detect if data from another user exists.
     */
    @Query("SELECT DISTINCT userId FROM entry WHERE userId IS NOT NULL")
    suspend fun getDistinctUserIds(): List<String>

    /**
     * Delete all entries from the database.
     * Used during account deletion to clear all local data.
     */
    @Query("DELETE FROM entry")
    suspend fun deleteAll()
}

/**
 * Data class for mood color entry count query results.
 */
data class MoodColorEntryCount(
    val moodColorId: Int,
    val entryCount: Int
)