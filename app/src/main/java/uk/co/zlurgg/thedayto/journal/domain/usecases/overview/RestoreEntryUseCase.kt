package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

/**
 * Restores a previously deleted entry.
 * Used by OverviewViewModel for undo functionality.
 */
class RestoreEntryUseCase(
    private val repository: EntryRepository
) {
    suspend operator fun invoke(entry: Entry) {
        repository.insertEntry(entry)
    }
}
