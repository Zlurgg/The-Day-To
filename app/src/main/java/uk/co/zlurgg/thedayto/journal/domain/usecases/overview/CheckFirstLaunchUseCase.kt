package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

/**
 * Use Case: Check if this is the user's first launch of the app
 *
 * Business Logic:
 * - Checks if this is the user's first time launching the Overview screen
 * - Used to determine if tutorial should be shown instead of entry reminder
 * - Part of the first-time user onboarding flow
 *
 * @param preferencesRepository Repository for accessing preference data
 */
class CheckFirstLaunchUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    /**
     * Execute the use case
     *
     * @return true if this is first launch, false otherwise
     */
    suspend operator fun invoke(): Boolean {
        return preferencesRepository.isFirstLaunch()
    }
}
