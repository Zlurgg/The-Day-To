package uk.co.zlurgg.thedayto.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
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

    override suspend fun getEntryById(id: Int): Entry? {
        return entries.find { it.id == id }
    }

    override suspend fun getEntryByDate(dateStamp: Long): Entry? {
        return entries.find { it.dateStamp == dateStamp }
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
