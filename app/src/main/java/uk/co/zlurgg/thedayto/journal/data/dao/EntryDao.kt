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
}