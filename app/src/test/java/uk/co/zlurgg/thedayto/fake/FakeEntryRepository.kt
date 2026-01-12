package uk.co.zlurgg.thedayto.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Fake implementation of EntryRepository for testing.
 * Stores entries in memory and provides synchronous access for test verification.
 * Uses StateFlow to simulate Room's reactive Flow behavior.
 *
 * Can optionally accept a FakeMoodColorRepository to simulate proper JOIN behavior.
 */
class FakeEntryRepository(
    private val moodColorRepository: FakeMoodColorRepository? = null
) : EntryRepository {

    // In-memory storage for testing (use StateFlow to emit on changes like Room)
    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    private var nextId = 1

    /** Set to true to make deleteEntry throw an exception (for error handling tests) */
    var shouldThrowOnDelete = false

    override fun getEntries(): Flow<List<Entry>> = _entries

    override fun getEntriesWithMoodColors(): Flow<List<EntryWithMoodColor>> {
        // Use map to maintain reactive behavior like Room
        return _entries.map { entries ->
            entries.map { entry ->
                // If moodColorRepository is provided, do a proper join (simulates real DB behavior)
                val moodColor = moodColorRepository?.getMoodColorByIdSync(entry.moodColorId)

                EntryWithMoodColor(
                    id = entry.id,
                    moodColorId = entry.moodColorId,
                    moodName = moodColor?.mood ?: "Test Mood",  // Use actual mood name from join
                    moodColor = moodColor?.color ?: "4CAF50",   // Use actual color from join
                    content = entry.content,
                    dateStamp = entry.dateStamp
                )
            }
        }
    }

    override fun getEntriesForMonth(month: Int, year: Int): Flow<List<EntryWithMoodColor>> {
        require(month in 1..12) { "Month must be between 1 and 12, got: $month" }
        require(year > 0) { "Year must be positive, got: $year" }

        val (startEpoch, endEpoch) = getMonthRange(month, year)

        return _entries.map { entries ->
            entries
                .filter { it.dateStamp >= startEpoch && it.dateStamp < endEpoch }
                .map { entry ->
                    val moodColor = moodColorRepository?.getMoodColorByIdSync(entry.moodColorId)
                    EntryWithMoodColor(
                        id = entry.id,
                        moodColorId = entry.moodColorId,
                        moodName = moodColor?.mood ?: "Test Mood",
                        moodColor = moodColor?.color ?: "4CAF50",
                        content = entry.content,
                        dateStamp = entry.dateStamp
                    )
                }
        }
    }

    private fun getMonthRange(month: Int, year: Int): Pair<Long, Long> {
        val startOfMonth = LocalDate.of(year, month, 1)
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC)

        val startOfNextMonth = LocalDate.of(year, month, 1)
            .plusMonths(1)
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC)

        return Pair(startOfMonth, startOfNextMonth)
    }

    override suspend fun getEntryById(id: Int): Result<Entry?, DataError.Local> {
        return Result.Success(_entries.value.find { it.id == id })
    }

    override suspend fun getEntryWithMoodColorById(id: Int): Result<EntryWithMoodColor?, DataError.Local> {
        val entry = _entries.value.find { it.id == id }
            ?: return Result.Success(null)
        val moodColor = moodColorRepository?.getMoodColorByIdSync(entry.moodColorId)
        return Result.Success(
            EntryWithMoodColor(
                id = entry.id,
                moodColorId = entry.moodColorId,
                moodName = moodColor?.mood ?: "Test Mood",
                moodColor = moodColor?.color ?: "4CAF50",
                content = entry.content,
                dateStamp = entry.dateStamp
            )
        )
    }

    override suspend fun getEntryByDate(date: Long): Result<Entry?, DataError.Local> {
        return Result.Success(_entries.value.find { it.dateStamp == date })
    }

    override suspend fun getEntryWithMoodColorByDate(date: Long): Result<EntryWithMoodColor?, DataError.Local> {
        val entry = _entries.value.find { it.dateStamp == date }
            ?: return Result.Success(null)
        val moodColor = moodColorRepository?.getMoodColorByIdSync(entry.moodColorId)
        return Result.Success(
            EntryWithMoodColor(
                id = entry.id,
                moodColorId = entry.moodColorId,
                moodName = moodColor?.mood ?: "Test Mood",
                moodColor = moodColor?.color ?: "4CAF50",
                content = entry.content,
                dateStamp = entry.dateStamp
            )
        )
    }

    override suspend fun insertEntry(entry: Entry): EmptyResult<DataError.Local> {
        val entryWithId = if (entry.id == null) {
            entry.copy(id = nextId++)
        } else {
            entry
        }
        val currentList = _entries.value.toMutableList()
        currentList.removeIf { it.id == entryWithId.id }
        currentList.add(entryWithId)
        _entries.value = currentList
        return Result.Success(Unit)
    }

    override suspend fun deleteEntry(entry: Entry): EmptyResult<DataError.Local> {
        if (shouldThrowOnDelete) {
            return Result.Error(DataError.Local.DATABASE_ERROR)
        }
        val currentList = _entries.value.toMutableList()
        currentList.removeIf { it.id == entry.id }
        _entries.value = currentList
        return Result.Success(Unit)
    }

    override suspend fun updateEntry(entry: Entry): EmptyResult<DataError.Local> {
        val currentList = _entries.value.toMutableList()
        currentList.removeIf { it.id == entry.id }
        currentList.add(entry)
        _entries.value = currentList
        return Result.Success(Unit)
    }

    override fun getMoodColorEntryCounts(): Flow<Map<Int, Int>> {
        return _entries.map { entries ->
            entries.groupingBy { it.moodColorId }.eachCount()
        }
    }

    /**
     * Helper method to reset the repository to its initial state.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        _entries.value = emptyList()
        nextId = 1
        shouldThrowOnDelete = false
    }

    /**
     * Helper method to get all entries synchronously for test assertions.
     */
    fun getEntriesSync(): List<Entry> {
        return _entries.value.toList()
    }
}
