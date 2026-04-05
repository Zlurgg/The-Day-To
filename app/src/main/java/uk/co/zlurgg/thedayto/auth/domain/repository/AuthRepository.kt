package uk.co.zlurgg.thedayto.auth.domain.repository

import uk.co.zlurgg.thedayto.auth.domain.model.CredentialProvider
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
     * Signs in using credentials provided by the caller.
     *
     * The credentialProvider lambda allows the UI layer (which has Activity context)
     * to provide credentials without passing Android types through the domain layer.
     *
     * @param credentialProvider Suspend lambda that fetches Google credentials
     * @return Result containing UserData on success or DataError.Auth on failure
     */
    suspend fun signIn(credentialProvider: CredentialProvider): Result<UserData, DataError.Auth>

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
     * account deletion. This method allows re-authentication using the same
     * credential provider pattern as signIn.
     *
     * @param credentialProvider Suspend lambda that fetches Google credentials
     * @return EmptyResult with success or DataError.Auth on failure
     */
    suspend fun reauthenticate(credentialProvider: CredentialProvider): EmptyResult<DataError.Auth>
}
