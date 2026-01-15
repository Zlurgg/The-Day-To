package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository
import uk.co.zlurgg.thedayto.auth.domain.service.DevAuthService
import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.Result
import io.github.zlurgg.core.domain.result.onSuccess

/**
 * Initiates development sign-in via Firebase Auth Emulator.
 *
 * @param devAuthService Service for dev authentication
 * @param authStateRepository Repository for managing sign-in state
 */
class DevSignInUseCase(
    private val devAuthService: DevAuthService,
    private val authStateRepository: AuthStateRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<UserData, DataError.Auth> {
        return devAuthService.signInWithEmailPassword(email, password)
            .onSuccess {
                authStateRepository.setSignedInState(true)
            }
    }

    fun isAvailable(): Boolean = devAuthService.isAvailable()
}
