package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.EmptyResult
import io.github.zlurgg.core.domain.result.Result

/**
 * Fake implementation of AuthRepository for testing.
 * Allows controlling sign-in success/failure scenarios.
 */
class FakeAuthRepository : AuthRepository {

    // In-memory storage for testing
    private var currentUser: UserData? = null

    // Control sign-in behavior for testing
    var shouldReturnError = false
    var authError: DataError.Auth = DataError.Auth.FAILED

    override suspend fun signIn(): Result<UserData, DataError.Auth> {
        return if (shouldReturnError) {
            Result.Error(authError)
        } else {
            val userData = UserData(
                userId = "test_user_123",
                username = "Test User",
                profilePictureUrl = "https://example.com/profile.jpg"
            )
            currentUser = userData
            Result.Success(userData)
        }
    }

    override suspend fun signOut(): EmptyResult<DataError.Auth> {
        currentUser = null
        return Result.Success(Unit)
    }

    override fun getSignedInUser(): UserData? {
        return currentUser
    }

    /**
     * Helper method to set a signed-in user for testing.
     */
    fun setSignedInUser(user: UserData?) {
        currentUser = user
    }

    /**
     * Helper method to reset all values to defaults.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        currentUser = null
        shouldReturnError = false
        authError = DataError.Auth.FAILED
    }
}
