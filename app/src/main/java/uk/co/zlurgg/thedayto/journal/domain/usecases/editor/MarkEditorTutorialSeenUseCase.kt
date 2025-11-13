package uk.co.zlurgg.thedayto.journal.domain.usecases.editor

import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

/**
 * Use Case: Mark that the user has seen the editor tutorial
 *
 * Business Logic:
 * - Marks that the editor tutorial has been displayed and dismissed
 * - Called after the tutorial is shown on first entry creation
 * - Ensures the tutorial is only shown once
 *
 * @param preferencesRepository Repository for storing preference data
 */
class MarkEditorTutorialSeenUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    /**
     * Execute the use case
     */
    suspend operator fun invoke() {
        preferencesRepository.markEditorTutorialSeen()
    }
}
