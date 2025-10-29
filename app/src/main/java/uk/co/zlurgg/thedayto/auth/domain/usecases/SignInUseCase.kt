package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.auth.domain.model.SignInResult
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository

/**
 * Initiates Google Sign-In flow.
 * Returns SignInResult with user data or error message.
 *
 * Follows Clean Architecture:
 * - No Android Context dependency in domain layer
 * - Uses repository abstraction instead of data layer service
 * - Pure business logic for coordinating sign-in and state updates
 */
class SignInUseCase(
    private val authRepository: AuthRepository,
    private val authStateRepository: AuthStateRepository
) {
    suspend operator fun invoke(): SignInResult {
        val result = authRepository.signIn()

        if (result.data != null) {
            // Save sign-in state on success
            authStateRepository.setSignedInState(true)
        }

        return result
    }
}
