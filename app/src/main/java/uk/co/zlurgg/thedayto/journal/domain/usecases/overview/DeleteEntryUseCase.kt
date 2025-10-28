package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

class DeleteEntryUseCase(
    private val repository: EntryRepository
) {
    suspend operator fun invoke(entry: Entry) {
        repository.deleteEntry(entry)
    }
}