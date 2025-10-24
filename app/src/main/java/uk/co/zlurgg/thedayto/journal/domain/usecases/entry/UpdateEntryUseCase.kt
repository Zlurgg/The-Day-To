package uk.co.zlurgg.thedayto.journal.domain.usecases.entry

import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

class UpdateEntryUseCase(
    private val repository: EntryRepository
) {
    suspend operator fun invoke(entry: Entry) {
        return repository.updateEntry(entry)
    }
}