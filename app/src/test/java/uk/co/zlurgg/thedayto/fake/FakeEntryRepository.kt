package uk.co.zlurgg.thedayto.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

/**
 * Fake implementation of EntryRepository for testing.
 * Stores entries in memory and provides synchronous access for test verification.
 */
class FakeEntryRepository : EntryRepository {

    // In-memory storage for testing
    private val entries = mutableListOf<Entry>()
    private var nextId = 1

    override fun getEntries(): Flow<List<Entry>> = flow {
        emit(entries.toList())
    }

    override fun getEntriesWithMoodColors(): Flow<List<EntryWithMoodColor>> = flow {
        emit(entries.map { entry ->
            EntryWithMoodColor(
                id = entry.id,
                moodColorId = entry.moodColorId,
                moodName = "Test Mood",  // Dummy data for testing
                moodColor = "4CAF50",    // Green color
                content = entry.content,
                dateStamp = entry.dateStamp
            )
        })
    }

    override suspend fun getEntryById(id: Int): Entry? {
        return entries.find { it.id == id }
    }

    override suspend fun getEntryWithMoodColorById(id: Int): EntryWithMoodColor? {
        val entry = entries.find { it.id == id } ?: return null
        return EntryWithMoodColor(
            id = entry.id,
            moodColorId = entry.moodColorId,
            moodName = "Test Mood",
            moodColor = "4CAF50",
            content = entry.content,
            dateStamp = entry.dateStamp
        )
    }

    override suspend fun getEntryByDate(date: Long): Entry? {
        return entries.find { it.dateStamp == date }
    }

    override suspend fun getEntryWithMoodColorByDate(date: Long): EntryWithMoodColor? {
        val entry = entries.find { it.dateStamp == date } ?: return null
        return EntryWithMoodColor(
            id = entry.id,
            moodColorId = entry.moodColorId,
            moodName = "Test Mood",
            moodColor = "4CAF50",
            content = entry.content,
            dateStamp = entry.dateStamp
        )
    }

    override suspend fun insertEntry(entry: Entry) {
        val entryWithId = if (entry.id == null) {
            entry.copy(id = nextId++)
        } else {
            entry
        }
        entries.removeIf { it.id == entryWithId.id }
        entries.add(entryWithId)
    }

    override suspend fun deleteEntry(entry: Entry) {
        entries.removeIf { it.id == entry.id }
    }

    override suspend fun updateEntry(entry: Entry) {
        entries.removeIf { it.id == entry.id }
        entries.add(entry)
    }

    /**
     * Helper method to reset the repository to its initial state.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        entries.clear()
        nextId = 1
    }

    /**
     * Helper method to get all entries synchronously for test assertions.
     */
    fun getEntriesSync(): List<Entry> {
        return entries.toList()
    }
}
