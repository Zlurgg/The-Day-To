package uk.co.zlurgg.thedayto.journal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.journal.data.model.EntryEntity

@Dao
interface EntryDao {

    @Query("SELECT * FROM entry")
    fun getEntries(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entry WHERE id = :id")
    suspend fun getEntryById(id: Int): EntryEntity?

    @Query("SELECT * FROM entry WHERE dateStamp = :date")
    suspend fun getEntryByDate(date: Long): EntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: EntryEntity)

    @Delete
    suspend fun deleteEntry(entry: EntryEntity)

    @Update
    suspend fun updateEntry(entry: EntryEntity)
}