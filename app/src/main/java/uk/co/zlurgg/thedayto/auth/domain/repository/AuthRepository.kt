package uk.co.zlurgg.thedayto.auth.domain.repository

import uk.co.zlurgg.thedayto.auth.domain.model.SignInResult
import uk.co.zlurgg.thedayto.auth.domain.model.UserData

/**
 * Repository interface for authentication operations.
 *
 * Follows Clean Architecture principles:
 * - Domain layer defines the contract (this interface)
 * - Data layer implements with GoogleAuthUiClient
 * - No Android/framework dependencies in this interface
 *
 * This abstracts away the authentication provider (Google, Firebase, etc.)
 * from the domain layer, making it testable and provider-agnostic.
 */
interface AuthRepository {
    /**
     * Initiates the sign-in flow.
     *
     * @return SignInResult containing user data on success or error message on failure
     */
    suspend fun signIn(): SignInResult

    /**
     * Signs out the current user.
     * Clears authentication state from the provider.
     */
    suspend fun signOut()

    /**
     * Gets the currently signed-in user.
     *
     * @return UserData if user is signed in, null otherwise
     */
    fun getSignedInUser(): UserData?
}
