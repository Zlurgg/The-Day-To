package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

/**
 * Use Case: Mark that the first launch setup has been completed
 *
 * Business Logic:
 * - Marks that the user has completed their first Overview screen visit
 * - Called after tutorial dialog is shown to first-time users
 * - Ensures tutorial is only shown once
 *
 * @param preferencesRepository Repository for storing preference data
 */
class MarkFirstLaunchCompleteUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    /**
     * Execute the use case
     */
    suspend operator fun invoke() {
        preferencesRepository.markFirstLaunchComplete()
    }
}
