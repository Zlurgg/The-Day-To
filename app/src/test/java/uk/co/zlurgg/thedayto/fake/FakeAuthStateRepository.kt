package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository

/**
 * Fake implementation of AuthStateRepository for testing.
 * Stores data in memory instead of SharedPreferences.
 */
class FakeAuthStateRepository : AuthStateRepository {

    // In-memory storage for testing
    private var signedInState: Boolean = false

    override fun setSignedInState(isSignedIn: Boolean) {
        this.signedInState = isSignedIn
    }

    override fun getSignedInState(): Boolean {
        return signedInState
    }

    /**
     * Helper method to reset all values to defaults.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        signedInState = false
    }
}
