package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.journal.domain.repository.PreferencesRepository

/**
 * Use Case: Check if user has seen the welcome dialog
 *
 * Determines whether to display the welcome dialog on the sign-in screen.
 * First-time users see the welcome dialog before signing in, providing
 * context about the app before authentication.
 *
 * @param preferencesRepository Repository for accessing welcome dialog state
 */
class CheckWelcomeDialogSeenUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(): Boolean {
        return preferencesRepository.hasSeenWelcomeDialog()
    }
}
