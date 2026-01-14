package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.service.DevAuthService
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result

/**
 * Fake implementation of DevAuthService for testing.
 * Allows controlling dev sign-in success/failure scenarios.
 */
class FakeDevAuthService : DevAuthService {

    // Control sign-in behavior for testing
    var shouldReturnError = false
    var authError: DataError.Auth = DataError.Auth.FAILED
    var isDevAvailable = true

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Result<UserData, DataError.Auth> {
        return if (shouldReturnError) {
            Result.Error(authError)
        } else {
            Result.Success(
                UserData(
                    userId = "dev_test_user",
                    username = "Dev Test User",
                    profilePictureUrl = null
                )
            )
        }
    }

    override fun isAvailable(): Boolean = isDevAvailable

    /**
     * Helper method to reset all values to defaults.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        shouldReturnError = false
        authError = DataError.Auth.FAILED
        isDevAvailable = true
    }
}
