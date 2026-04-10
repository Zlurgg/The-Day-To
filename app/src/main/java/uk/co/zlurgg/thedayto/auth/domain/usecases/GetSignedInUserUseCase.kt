package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository

/**
 * Gets the currently signed-in user.
 *
 * Follows Clean Architecture:
 * - Wraps repository call to prevent direct repository access from ViewModels
 * - Pure domain operation with no side effects
 *
 * @return UserData if user is signed in, null otherwise
 */
class GetSignedInUserUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): UserData? = authRepository.getSignedInUser()
}
