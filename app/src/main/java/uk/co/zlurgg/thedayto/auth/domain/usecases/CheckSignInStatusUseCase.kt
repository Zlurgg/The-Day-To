package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository

/**
 * Checks if user is currently signed in.
 * Verifies both local state and authentication provider state.
 *
 * Follows Clean Architecture:
 * - Uses repository abstraction instead of data layer service
 * - Pure business logic for verifying sign-in status
 *
 * @return true if user is signed in, false otherwise
 */
class CheckSignInStatusUseCase(
    private val authRepository: AuthRepository,
    private val authStateRepository: AuthStateRepository
) {
    operator fun invoke(): Boolean {
        val isSignedIn = authStateRepository.getSignedInState()
        val currentUser = authRepository.getSignedInUser()

        return isSignedIn && currentUser != null
    }
}
