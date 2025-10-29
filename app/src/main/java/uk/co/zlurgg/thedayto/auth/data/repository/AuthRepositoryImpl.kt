package uk.co.zlurgg.thedayto.auth.data.repository

import android.content.Context
import uk.co.zlurgg.thedayto.auth.data.service.GoogleAuthUiClient
import uk.co.zlurgg.thedayto.auth.domain.model.SignInResult
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository

/**
 * Implementation of AuthRepository using GoogleAuthUiClient.
 *
 * This adapter:
 * - Handles Android Context dependencies at the data layer
 * - Wraps GoogleAuthUiClient to implement domain interface
 * - Keeps domain layer clean and framework-independent
 *
 * @param context Application context for authentication operations
 */
class AuthRepositoryImpl(
    private val context: Context
) : AuthRepository {

    private val googleAuthUiClient = GoogleAuthUiClient(context)

    override suspend fun signIn(): SignInResult {
        // Use application context for credential UI
        // In Android, the Credential Manager needs a Context to display the sign-in UI
        return googleAuthUiClient.signIn(context)
    }

    override suspend fun signOut() {
        googleAuthUiClient.signOut()
    }

    override fun getSignedInUser(): UserData? {
        return googleAuthUiClient.getSignedInUser()
    }
}
