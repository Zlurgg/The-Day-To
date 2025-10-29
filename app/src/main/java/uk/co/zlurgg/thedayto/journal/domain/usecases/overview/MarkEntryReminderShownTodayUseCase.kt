package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.journal.domain.repository.PreferencesRepository

/**
 * Use Case: Mark that entry reminder dialog has been shown today
 *
 * Business Logic:
 * - Records that the reminder dialog was displayed on the current day
 * - Prevents showing the dialog multiple times in a single day
 * - Respects user's choice to dismiss without creating entry
 *
 * @param preferencesRepository Repository for persisting preference data
 */
class MarkEntryReminderShownTodayUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    /**
     * Execute the use case
     *
     * Records the current date to prevent reminder from showing again today
     */
    suspend operator fun invoke() {
        preferencesRepository.markEntryReminderShownToday()
    }
}
