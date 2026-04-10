package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.auth.domain.model.CredentialProvider
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result

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

    // Separate control for deleteAccount to test partial failure scenarios
    var deleteAccountError: DataError.Auth? = null

    // Call tracking for verification
    var signOutCalled = false

    override suspend fun signIn(
        credentialProvider: CredentialProvider,
    ): Result<UserData, DataError.Auth> {
        // In tests, we ignore the credentialProvider and use our configured behavior
        return if (shouldReturnError) {
            Result.Error(authError)
        } else {
            val userData = UserData(
                userId = "test_user_123",
                username = "Test User",
                profilePictureUrl = "https://example.com/profile.jpg",
            )
            currentUser = userData
            Result.Success(userData)
        }
    }

    override suspend fun signOut(): EmptyResult<DataError.Auth> {
        signOutCalled = true
        currentUser = null
        return Result.Success(Unit)
    }

    override fun getSignedInUser(): UserData? {
        return currentUser
    }

    override suspend fun deleteAccount(): EmptyResult<DataError.Auth> {
        // Use specific deleteAccountError if set, otherwise fall back to general error behavior
        deleteAccountError?.let { return Result.Error(it) }

        return if (shouldReturnError) {
            Result.Error(authError)
        } else {
            currentUser = null
            Result.Success(Unit)
        }
    }

    override suspend fun reauthenticate(
        credentialProvider: CredentialProvider,
    ): EmptyResult<DataError.Auth> {
        return if (shouldReturnError) {
            Result.Error(authError)
        } else {
            Result.Success(Unit)
        }
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
        deleteAccountError = null
        signOutCalled = false
    }
}
