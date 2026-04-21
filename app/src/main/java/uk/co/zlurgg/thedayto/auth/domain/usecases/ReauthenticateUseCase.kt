package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.auth.domain.model.IdToken
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult

/**
 * Re-authenticates the current user with fresh credentials.
 *
 * Firebase requires recent authentication for sensitive operations like
 * account deletion.
 */
class ReauthenticateUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(idToken: IdToken): EmptyResult<DataError.Auth> =
        authRepository.reauthenticate(idToken)
}
