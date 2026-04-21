package uk.co.zlurgg.thedayto.auth.data.service

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import uk.co.zlurgg.thedayto.auth.data.error.AuthErrorMapper
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result

/**
 * Google Auth client for Firebase authentication.
 *
 * This class handles Firebase sign-in with Google credentials.
 * Credential fetching (which requires Activity context) is handled
 * separately by GoogleCredentialFetcher.
 *
 * Split responsibilities:
 * - GoogleCredentialFetcher: Fetches credentials (needs Activity)
 * - GoogleAuthUiClient: Firebase sign-in (needs Application context only)
 */
class GoogleAuthUiClient(
    private val context: Context,
) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    /**
     * Signs in to Firebase using a Google ID token.
     *
     * This method does not require Activity context - the credential
     * has already been obtained via GoogleCredentialFetcher.
     *
     * @param idToken The Google ID token from GoogleCredentialFetcher
     * @return Result with UserData on success or DataError.Auth on failure
     */
    suspend fun signInWithCredential(idToken: String): Result<UserData, DataError.Auth> {
        return AuthErrorMapper.safeAuthCall(TAG) {
            val googleCredentials = GoogleAuthProvider.getCredential(idToken, null)
            val user = auth.signInWithCredential(googleCredentials).await().user
                ?: error("Firebase returned null user")

            UserData(
                userId = user.uid,
                username = user.displayName,
                profilePictureUrl = user.photoUrl?.toString(),
            )
        }
    }

    /**
     * Signs out the current user from both Firebase and Google.
     */
    suspend fun signOut(): EmptyResult<DataError.Auth> {
        return AuthErrorMapper.safeAuthCall(TAG) {
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest(),
            )
            auth.signOut()
            Timber.d("User signed out successfully")
        }
    }

    /**
     * Gets the currently signed-in user, if any.
     */
    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString(),
        )
    }

    /**
     * Deletes the current user's Firebase Auth account.
     *
     * Also clears credential state to ensure clean sign-out.
     */
    suspend fun deleteAccount(): EmptyResult<DataError.Auth> {
        return AuthErrorMapper.safeAuthCall(TAG) {
            val user = auth.currentUser ?: error("No user signed in")
            user.delete().await()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Timber.d("User account deleted successfully")
        }
    }

    /**
     * Re-authenticates the current user with a fresh Google credential.
     *
     * Required before sensitive operations like account deletion if the
     * user hasn't signed in recently.
     */
    suspend fun reauthenticateWithCredential(idToken: String): EmptyResult<DataError.Auth> {
        return AuthErrorMapper.safeAuthCall(TAG) {
            val user = auth.currentUser ?: error("No user signed in")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            user.reauthenticate(credential).await()
            Timber.d("User re-authenticated successfully")
        }
    }

    companion object {
        private const val TAG = "GoogleAuthUiClient"
    }
}
