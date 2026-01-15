package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository
import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.Result
import io.github.zlurgg.core.domain.result.onSuccess

/**
 * Initiates Google Sign-In flow.
 * Returns Result with UserData on success or DataError.Auth on failure.
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
    suspend operator fun invoke(): Result<UserData, DataError.Auth> {
        return authRepository.signIn()
            .onSuccess {
                // Save sign-in state on success
                authStateRepository.setSignedInState(true)
            }
    }
}
