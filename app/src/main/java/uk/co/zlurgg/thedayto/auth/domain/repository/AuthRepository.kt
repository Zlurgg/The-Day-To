package uk.co.zlurgg.thedayto.auth.domain.repository

import uk.co.zlurgg.thedayto.auth.domain.model.IdToken
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
     * Signs in using a Google ID token.
     *
     * @param idToken Type-safe wrapper for the Google ID token
     * @return Result containing UserData on success or DataError.Auth on failure
     */
    suspend fun signIn(idToken: IdToken): Result<UserData, DataError.Auth>

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

    /**
     * Deletes the current user's Firebase Auth account.
     *
     * This is a sensitive operation that may require recent authentication.
     * If the user hasn't signed in recently, this may return REQUIRES_RECENT_LOGIN.
     *
     * @return EmptyResult with success or DataError.Auth on failure
     */
    suspend fun deleteAccount(): EmptyResult<DataError.Auth>

    /**
     * Re-authenticates with fresh credentials (required for sensitive operations).
     *
     * Firebase requires recent authentication for sensitive operations like
     * account deletion.
     *
     * @param idToken Type-safe wrapper for the Google ID token
     * @return EmptyResult with success or DataError.Auth on failure
     */
    suspend fun reauthenticate(idToken: IdToken): EmptyResult<DataError.Auth>
}
