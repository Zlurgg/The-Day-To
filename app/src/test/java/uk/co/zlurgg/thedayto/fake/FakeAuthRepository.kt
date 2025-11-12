package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.auth.domain.model.SignInResult
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository

/**
 * Fake implementation of AuthRepository for testing.
 * Allows controlling sign-in success/failure scenarios.
 */
class FakeAuthRepository : AuthRepository {

    // In-memory storage for testing
    private var currentUser: UserData? = null

    // Control sign-in behavior for testing
    var shouldReturnError = false
    var errorMessage = "Sign-in failed"

    override suspend fun signIn(): SignInResult {
        return if (shouldReturnError) {
            SignInResult(data = null, errorMessage = errorMessage)
        } else {
            val userData = UserData(
                userId = "test_user_123",
                username = "Test User",
                profilePictureUrl = "https://example.com/profile.jpg"
            )
            currentUser = userData
            SignInResult(data = userData, errorMessage = null)
        }
    }

    override suspend fun signOut() {
        currentUser = null
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
        errorMessage = "Sign-in failed"
    }
}
