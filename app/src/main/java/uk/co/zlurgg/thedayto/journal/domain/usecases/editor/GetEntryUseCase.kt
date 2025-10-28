package uk.co.zlurgg.thedayto.journal.domain.usecases.editor

import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

class GetEntryUseCase(
    private val repository: EntryRepository
) {
    suspend operator fun invoke(id: Int): Entry? {
        return repository.getEntryById(id)
    }
}