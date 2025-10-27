package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.journal.domain.model.Entry

/**
 * Fake implementation of GetEntryByDateUseCase for testing.
 * Allows tests to control which dates have entries.
 */
class FakeGetEntryByDateUseCase {

    // Map of datestamp -> Entry for testing
    private val entries = mutableMapOf<Long, Entry>()

    suspend operator fun invoke(datestamp: Long): Entry? {
        return entries[datestamp]
    }

    /**
     * Add an entry for a specific date (for test setup)
     */
    fun addEntry(datestamp: Long, entry: Entry) {
        entries[datestamp] = entry
    }

    /**
     * Remove entry for a specific date
     */
    fun removeEntry(datestamp: Long) {
        entries.remove(datestamp)
    }

    /**
     * Clear all entries
     */
    fun reset() {
        entries.clear()
    }
}
