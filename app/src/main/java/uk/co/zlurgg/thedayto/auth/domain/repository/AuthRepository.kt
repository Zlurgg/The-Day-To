package uk.co.zlurgg.thedayto.auth.domain.repository

import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result

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
     * @return Result containing UserData on success or DataError.Auth on failure
     */
    suspend fun signIn(): Result<UserData, DataError.Auth>

    /**
     * Signs out the current user.
     * Clears authentication state from the provider.
     */
    suspend fun signOut(): EmptyResult<DataError.Auth>

    /**
     * Gets the currently signed-in user.
     *
     * @return UserData if user is signed in, null otherwise
     */
    fun getSignedInUser(): UserData?
}
