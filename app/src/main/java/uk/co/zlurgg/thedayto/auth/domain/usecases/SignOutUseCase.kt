package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.auth.data.service.GoogleAuthUiClient
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository

/**
 * Signs out the current user.
 * Clears Google authentication and updates local sign-in state.
 */
class SignOutUseCase(
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val authStateRepository: AuthStateRepository
) {
    suspend operator fun invoke() {
        // Sign out from Google Auth
        googleAuthUiClient.signOut()

        // Clear sign-in state
        authStateRepository.setSignedInState(false)
    }
}
