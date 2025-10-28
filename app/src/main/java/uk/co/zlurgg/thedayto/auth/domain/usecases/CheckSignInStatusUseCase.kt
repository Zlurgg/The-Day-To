package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.auth.data.service.GoogleAuthUiClient
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository

/**
 * Checks if user is currently signed in.
 * Verifies both local state and Google authentication.
 *
 * @return true if user is signed in, false otherwise
 */
class CheckSignInStatusUseCase(
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val authStateRepository: AuthStateRepository
) {
    operator fun invoke(): Boolean {
        val isSignedIn = authStateRepository.getSignedInState()
        val currentUser = googleAuthUiClient.getSignedInUser()

        return isSignedIn && currentUser != null
    }
}
