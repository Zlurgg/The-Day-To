package uk.co.zlurgg.thedayto.auth.domain.service

import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.Result

/**
 * Service interface for development/testing authentication.
 *
 * In debug builds: Connects to Firebase Auth Emulator
 * In release builds: No-op implementation
 */
interface DevAuthService {
    /**
     * Sign in with email/password to Firebase Auth Emulator.
     *
     * @param email Test user email
     * @param password Test user password
     * @return Result with UserData on success or DataError.Auth on failure
     */
    suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Result<UserData, DataError.Auth>

    /**
     * Whether dev authentication is available.
     * @return true in debug builds, false in release
     */
    fun isAvailable(): Boolean
}
