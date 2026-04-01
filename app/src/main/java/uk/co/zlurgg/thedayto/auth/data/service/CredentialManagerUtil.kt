package uk.co.zlurgg.thedayto.auth.data.service

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import timber.log.Timber
import uk.co.zlurgg.thedayto.auth.domain.model.GoogleCredential
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
import java.util.concurrent.CancellationException

/**
 * Utility for fetching Google credentials using Credential Manager.
 *
 * This object isolates the Activity-dependent credential fetching logic.
 * The Activity reference is only captured in the lambda scope and never stored.
 *
 * Following Google's recommendation:
 * - Never pass Activity to ViewModel
 * - Use callback/lambda pattern for credential operations
 * - Activity reference captured only in scope, no memory leak risk
 */
object CredentialManagerUtil {

    private const val TAG = "CredentialManagerUtil"

    /**
     * Fetches Google credentials using the Credential Manager API.
     *
     * @param activity The Activity context (required for credential UI)
     * @param serverClientId The OAuth 2.0 web client ID
     * @return Result with GoogleCredential on success or DataError.Auth on failure
     */
    suspend fun getGoogleCredential(
        activity: Activity,
        serverClientId: String
    ): Result<GoogleCredential, DataError.Auth> {
        return try {
            val credentialManager = CredentialManager.create(activity)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = activity
            )

            when (val credential = response.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Result.Success(GoogleCredential(googleIdTokenCredential.idToken))
                    } else {
                        Timber.e("$TAG: Unexpected credential type: ${credential.type}")
                        Result.Error(DataError.Auth.FAILED)
                    }
                }
                else -> {
                    Timber.e("$TAG: Unexpected credential class: ${credential::class.java.name}")
                    Result.Error(DataError.Auth.FAILED)
                }
            }
        } catch (e: GetCredentialCancellationException) {
            Timber.d(e, "$TAG: User cancelled sign-in")
            Result.Error(DataError.Auth.CANCELLED)
        } catch (e: NoCredentialException) {
            Timber.w(e, "$TAG: No credentials available")
            Result.Error(DataError.Auth.NO_CREDENTIAL)
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Error getting Google credential")
            if (e is CancellationException) throw e

            val error = when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    DataError.Auth.NETWORK_ERROR
                else -> DataError.Auth.FAILED
            }
            Result.Error(error)
        }
    }
}
