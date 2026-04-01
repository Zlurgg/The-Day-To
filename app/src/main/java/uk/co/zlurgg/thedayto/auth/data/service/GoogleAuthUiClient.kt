package uk.co.zlurgg.thedayto.auth.data.service

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import java.util.concurrent.CancellationException

/**
 * Google Auth client for Firebase authentication.
 *
 * This class handles Firebase sign-in with Google credentials.
 * Credential fetching (which requires Activity context) is handled
 * separately by CredentialManagerUtil.
 *
 * Split responsibilities:
 * - CredentialManagerUtil: Fetches credentials (needs Activity)
 * - GoogleAuthUiClient: Firebase sign-in (needs Application context only)
 */
class GoogleAuthUiClient(
    private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    /**
     * Signs in to Firebase using a Google ID token.
     *
     * This method does not require Activity context - the credential
     * has already been obtained via CredentialManagerUtil.
     *
     * @param idToken The Google ID token from CredentialManagerUtil
     * @return Result with UserData on success or DataError.Auth on failure
     */
    suspend fun signInWithCredential(idToken: String): Result<UserData, DataError.Auth> {
        return try {
            val googleCredentials = GoogleAuthProvider.getCredential(idToken, null)
            val user = auth.signInWithCredential(googleCredentials).await().user
                ?: return Result.Error(DataError.Auth.FAILED)

            Result.Success(
                UserData(
                    userId = user.uid,
                    username = user.displayName,
                    profilePictureUrl = user.photoUrl?.toString()
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error signing in with Google credential")
            if (e is CancellationException) throw e

            val error = when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    DataError.Auth.NETWORK_ERROR
                else -> DataError.Auth.FAILED
            }
            Result.Error(error)
        }
    }

    /**
     * Signs out the current user from both Firebase and Google.
     */
    suspend fun signOut(): EmptyResult<DataError.Auth> {
        return try {
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
            auth.signOut()
            Timber.d("User signed out successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error signing out")
            if (e is CancellationException) throw e
            Result.Error(DataError.Auth.FAILED)
        }
    }

    /**
     * Gets the currently signed-in user, if any.
     */
    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }
}
