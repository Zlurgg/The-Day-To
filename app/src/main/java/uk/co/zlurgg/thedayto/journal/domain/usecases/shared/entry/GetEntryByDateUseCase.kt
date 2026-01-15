package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry

import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

/**
 * Use case for retrieving an entry by its date.
 *
 * Used for checking if an entry exists for a specific date,
 * particularly useful for navigation logic (e.g., "has user created entry today?").
 *
 * Following Clean Architecture:
 * - Single responsibility: Get entry by date
 * - Independent of framework details
 * - Testable with fake repository
 *
 * @param repository EntryRepository for data access
 */
class GetEntryByDateUseCase(
    private val repository: EntryRepository
) {
    /**
     * Get entry for a specific date
     *
     * @param datestamp Unix timestamp (seconds) at start of day
     * @return Result containing Entry if exists for that date, null otherwise
     */
    suspend operator fun invoke(datestamp: Long): Result<Entry?, DataError.Local> {
        return repository.getEntryByDate(datestamp)
    }
}
