package uk.co.zlurgg.thedayto.journal.domain.repository

import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.journal.domain.model.Entry

// TODO: Add a note to this and MoodColorRepo explaining logic behind using flow
interface EntryRepository {
    fun getEntries(): Flow<List<Entry>>
    suspend fun getEntryById(id: Int): Entry?
    suspend fun getEntryByDate(date: Long): Entry?
    suspend fun insertEntry(entry: Entry)
    suspend fun deleteEntry(entry: Entry)
    suspend fun updateEntry(entry: Entry)

}