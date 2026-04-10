package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.auth.domain.model.CredentialProvider
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult

/**
 * Re-authenticates the current user with fresh credentials.
 *
 * Firebase requires recent authentication for sensitive operations like
 * account deletion. This UseCase wraps the repository call to maintain
 * Clean Architecture principles.
 *
 * @param credentialProvider Lambda that provides Google credentials
 * @return EmptyResult with success or DataError.Auth on failure
 */
class ReauthenticateUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(credentialProvider: CredentialProvider): EmptyResult<DataError.Auth> =
        authRepository.reauthenticate(credentialProvider)
}
