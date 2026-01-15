package uk.co.zlurgg.thedayto.journal.domain.repository

import kotlinx.coroutines.flow.Flow
import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.EmptyResult
import io.github.zlurgg.core.domain.result.Result
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

    /**
     * Get entries for a specific month and year.
     * Filters at the database level for optimal performance.
     *
     * This method moves the filtering logic from the UI layer to the data layer,
     * improving performance by querying only the necessary data and reducing memory usage.
     *
     * @param month Month value (1-12)
     * @param year Year value (e.g., 2024)
     * @return Flow of entries with mood colors for the specified month
     * @throws IllegalArgumentException if month is not in 1..12 or year is not positive
     */
    fun getEntriesForMonth(month: Int, year: Int): Flow<List<EntryWithMoodColor>>

    suspend fun getEntryById(id: Int): Result<Entry?, DataError.Local>
    suspend fun getEntryWithMoodColorById(id: Int): Result<EntryWithMoodColor?, DataError.Local>
    suspend fun getEntryByDate(date: Long): Result<Entry?, DataError.Local>
    suspend fun getEntryWithMoodColorByDate(date: Long): Result<EntryWithMoodColor?, DataError.Local>
    suspend fun insertEntry(entry: Entry): EmptyResult<DataError.Local>
    suspend fun deleteEntry(entry: Entry): EmptyResult<DataError.Local>
    suspend fun updateEntry(entry: Entry): EmptyResult<DataError.Local>

    /**
     * Get the count of entries for each mood color.
     * Used by the Mood Color Management screen to show usage statistics.
     *
     * @return Flow of map (moodColorId to entryCount)
     */
    fun getMoodColorEntryCounts(): Flow<Map<Int, Int>>
}