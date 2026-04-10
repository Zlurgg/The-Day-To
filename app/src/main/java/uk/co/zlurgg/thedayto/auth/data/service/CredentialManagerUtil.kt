package uk.co.zlurgg.thedayto.auth.data.service

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import timber.log.Timber
import uk.co.zlurgg.thedayto.auth.data.error.AuthErrorMapper
import uk.co.zlurgg.thedayto.auth.domain.model.GoogleCredential
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result

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
        serverClientId: String,
    ): Result<GoogleCredential, DataError.Auth> {
        return AuthErrorMapper.safeAuthCall(TAG) {
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
                context = activity,
            )

            extractGoogleCredential(response.credential)
        }
    }

    private fun extractGoogleCredential(credential: androidx.credentials.Credential): GoogleCredential {
        return when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    GoogleCredential(googleIdTokenCredential.idToken)
                } else {
                    Timber.e("$TAG: Unexpected credential type: ${credential.type}")
                    error("Unexpected credential type: ${credential.type}")
                }
            }

            else -> {
                Timber.e("$TAG: Unexpected credential class: ${credential::class.java.name}")
                error("Unexpected credential class: ${credential::class.java.name}")
            }
        }
    }
}
