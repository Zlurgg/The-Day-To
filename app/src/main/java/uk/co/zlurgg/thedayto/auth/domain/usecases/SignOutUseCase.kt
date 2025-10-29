package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository

/**
 * Signs out the current user.
 * Clears Google authentication and updates local sign-in state.
 *
 * Follows Clean Architecture:
 * - Uses repository abstraction instead of data layer service
 * - Pure business logic for coordinating sign-out operations
 */
class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val authStateRepository: AuthStateRepository
) {
    suspend operator fun invoke() {
        // Sign out from auth provider
        authRepository.signOut()

        // Clear sign-in state
        authStateRepository.setSignedInState(false)
    }
}
