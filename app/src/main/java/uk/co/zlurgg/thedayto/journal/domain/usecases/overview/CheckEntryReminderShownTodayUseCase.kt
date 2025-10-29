package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.journal.domain.repository.PreferencesRepository

/**
 * Use Case: Check if entry reminder dialog has been shown today
 *
 * Business Logic:
 * - Checks if the reminder dialog was already displayed on the current day
 * - Ensures the reminder is only shown once per day (gentle nudge approach)
 * - Part of the non-intrusive user experience flow
 *
 * @param preferencesRepository Repository for accessing preference data
 */
class CheckEntryReminderShownTodayUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    /**
     * Execute the use case
     *
     * @return true if reminder was already shown today, false otherwise
     */
    suspend operator fun invoke(): Boolean {
        return preferencesRepository.hasShownEntryReminderToday()
    }
}
