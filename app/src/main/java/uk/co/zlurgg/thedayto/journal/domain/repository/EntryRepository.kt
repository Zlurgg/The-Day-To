package uk.co.zlurgg.thedayto.journal.domain.repository

import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor

/**
 * Repository for managing journal entries.
 *
 * **Flow vs Suspend Functions:**
 * - Methods returning `Flow<List<T>>` (e.g., getEntries, getEntriesWithMoodColors):
 *   These provide reactive streams that automatically emit new values when the underlying
 *   data changes. Room database queries returning Flow are observed by the UI, ensuring
 *   the UI updates automatically when data is inserted, updated, or deleted.
 *
 * - Methods using `suspend` (e.g., getEntryById, insertEntry, deleteEntry):
 *   These are one-time operations that execute once and return a result. Use these for
 *   single queries or write operations where you don't need to observe changes.
 */
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