package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry

import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

class GetEntryUseCase(
    private val repository: EntryRepository
) {
    suspend operator fun invoke(id: Int): Result<Entry?, DataError.Local> {
        return repository.getEntryById(id)
    }
}