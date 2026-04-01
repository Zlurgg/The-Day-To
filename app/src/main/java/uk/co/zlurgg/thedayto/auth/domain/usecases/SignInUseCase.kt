package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.auth.domain.model.CredentialProvider
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.core.domain.result.onSuccess

/**
 * Initiates Google Sign-In flow.
 * Returns Result with UserData on success or DataError.Auth on failure.
 *
 * Follows Clean Architecture:
 * - No Android Context dependency in domain layer
 * - Uses CredentialProvider callback for credential fetching
 * - Pure business logic for coordinating sign-in and state updates
 */
class SignInUseCase(
    private val authRepository: AuthRepository,
    private val authStateRepository: AuthStateRepository
) {
    suspend operator fun invoke(
        credentialProvider: CredentialProvider
    ): Result<UserData, DataError.Auth> {
        return authRepository.signIn(credentialProvider)
            .onSuccess {
                authStateRepository.setSignedInState(true)
            }
    }
}
