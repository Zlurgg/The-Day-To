package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.auth.domain.model.IdToken
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.core.domain.result.onSuccess

/**
 * Initiates Google Sign-In flow.
 * Returns Result with UserData on success or DataError.Auth on failure.
 */
class SignInUseCase(
    private val authRepository: AuthRepository,
    private val authStateRepository: AuthStateRepository,
) {
    suspend operator fun invoke(idToken: IdToken): Result<UserData, DataError.Auth> {
        return authRepository.signIn(idToken)
            .onSuccess {
                authStateRepository.setSignedInState(true)
            }
    }
}
