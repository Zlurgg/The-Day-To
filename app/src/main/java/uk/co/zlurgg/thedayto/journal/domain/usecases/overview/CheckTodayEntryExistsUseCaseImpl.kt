package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.core.domain.result.getOrNull
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckTodayEntryExistsUseCase
import uk.co.zlurgg.thedayto.core.domain.util.DateUtils
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

/**
 * Implementation of CheckTodayEntryExistsUseCase.
 *
 * Checks if an entry exists for today's date by querying the EntryRepository.
 * This implementation is in the journal module to avoid coupling core to journal.
 *
 * Used by NotificationWorker to determine if a reminder notification should be sent.
 * If user has already logged their mood for today, no notification is sent.
 *
 * Following Clean Architecture:
 * - Implements interface from core (dependency inversion)
 * - Single responsibility: Check today's entry existence
 * - Depends on abstraction (EntryRepository interface)
 *
 * @param repository EntryRepository for data access
 */
class CheckTodayEntryExistsUseCaseImpl(
    private val repository: EntryRepository
) : CheckTodayEntryExistsUseCase {
    /**
     * Check if an entry exists for today's date.
     *
     * Uses DateUtils to get today's start epoch (midnight) and queries
     * the repository for an entry with that date.
     *
     * @return true if an entry exists for today, false otherwise
     */
    override suspend fun invoke(): Boolean {
        val todayEpoch = DateUtils.getTodayStartEpoch()
        val entry = repository.getEntryByDate(todayEpoch).getOrNull()
        return entry != null
    }
}