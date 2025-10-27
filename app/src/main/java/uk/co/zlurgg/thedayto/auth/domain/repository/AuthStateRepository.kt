package uk.co.zlurgg.thedayto.auth.domain.repository

/**
 * Repository interface for managing authentication state.
 *
 * Stores user sign-in state in SharedPreferences.
 * This is the single source of truth for authentication status.
 *
 * Following Clean Architecture:
 * - Domain layer defines the contract
 * - Data layer implements with SharedPreferences
 * - No framework dependencies in this interface
 */
interface AuthStateRepository {
    /**
     * Save the user's signed-in state
     * @param isSignedIn true if user is signed in, false otherwise
     */
    fun setSignedInState(isSignedIn: Boolean)

    /**
     * Get the user's signed-in state
     * @return true if user is signed in, false otherwise
     */
    fun getSignedInState(): Boolean
}
