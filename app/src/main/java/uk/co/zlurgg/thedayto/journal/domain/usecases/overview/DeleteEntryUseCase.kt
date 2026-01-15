package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

class DeleteEntryUseCase(
    private val repository: EntryRepository
) {
    suspend operator fun invoke(entry: Entry): EmptyResult<DataError.Local> {
        return repository.deleteEntry(entry)
    }
}