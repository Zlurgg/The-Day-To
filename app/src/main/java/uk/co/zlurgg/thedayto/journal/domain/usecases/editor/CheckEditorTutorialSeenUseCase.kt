package uk.co.zlurgg.thedayto.journal.domain.usecases.editor

import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

/**
 * Use Case: Check if the user has seen the editor tutorial
 *
 * Business Logic:
 * - Checks if the editor tutorial has been shown to the user
 * - Used to determine if the tutorial should be displayed on first entry creation
 * - Part of the first-time user onboarding flow
 *
 * @param preferencesRepository Repository for accessing preference data
 */
class CheckEditorTutorialSeenUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    /**
     * Execute the use case
     *
     * @return true if user has seen the editor tutorial, false otherwise
     */
    suspend operator fun invoke(): Boolean {
        return preferencesRepository.hasSeenEditorTutorial()
    }
}
