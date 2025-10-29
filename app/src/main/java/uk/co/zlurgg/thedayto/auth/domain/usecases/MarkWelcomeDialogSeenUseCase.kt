package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.journal.domain.repository.PreferencesRepository

/**
 * Use Case: Mark welcome dialog as seen
 *
 * Records that the user has dismissed the welcome dialog.
 * Prevents showing the welcome dialog on subsequent app launches.
 *
 * Called when user dismisses the first-time welcome dialog on the
 * sign-in screen.
 *
 * @param preferencesRepository Repository for persisting welcome dialog state
 */
class MarkWelcomeDialogSeenUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke() {
        preferencesRepository.markWelcomeDialogSeen()
    }
}
