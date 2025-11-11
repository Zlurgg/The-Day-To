package uk.co.zlurgg.thedayto.journal.domain.repository

import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor

// TODO: Add a note to this and MoodColorRepo explaining logic behind using flow
interface EntryRepository {
    fun getEntries(): Flow<List<Entry>>
    fun getEntriesWithMoodColors(): Flow<List<EntryWithMoodColor>>
    suspend fun getEntryById(id: Int): Entry?
    suspend fun getEntryWithMoodColorById(id: Int): EntryWithMoodColor?
    suspend fun getEntryByDate(date: Long): Entry?
    suspend fun getEntryWithMoodColorByDate(date: Long): EntryWithMoodColor?
    suspend fun insertEntry(entry: Entry)
    suspend fun deleteEntry(entry: Entry)
    suspend fun updateEntry(entry: Entry)

}