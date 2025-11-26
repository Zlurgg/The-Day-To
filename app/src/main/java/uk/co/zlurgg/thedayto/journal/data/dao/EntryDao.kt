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

    @Delete
    suspend fun deleteEntry(entry: EntryEntity)

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
}

/**
 * Data class for mood color entry count query results.
 */
data class MoodColorEntryCount(
    val moodColorId: Int,
    val entryCount: Int
)