package uk.co.zlurgg.thedayto.auth.data.service

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import timber.log.Timber
import uk.co.zlurgg.thedayto.auth.data.error.AuthErrorMapper
import uk.co.zlurgg.thedayto.auth.domain.model.IdToken
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result

/**
 * Fetches Google credentials using the Credential Manager API.
 *
 * Stateless object — no Koin registration needed. Wraps a single
 * Android API call; unit testing is an integration concern.
 */
object GoogleCredentialFetcher {

    private const val TAG = "GoogleCredentialFetcher"

    /**
     * Fetches a Google ID token using the Credential Manager API.
     *
     * @param activity The Activity context (required for credential UI)
     * @param serverClientId The OAuth 2.0 web client ID
     * @return Result with IdToken on success or DataError.Auth on failure
     */
    suspend fun fetch(
        activity: Activity,
        serverClientId: String,
    ): Result<IdToken, DataError.Auth> {
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

            extractIdToken(response.credential)
        }
    }

    private fun extractIdToken(credential: androidx.credentials.Credential): IdToken {
        return when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    IdToken(googleIdTokenCredential.idToken)
                } else {
                    Timber.tag(TAG).e("Unexpected credential type: %s", credential.type)
                    error("Unexpected credential type: ${credential.type}")
                }
            }

            else -> {
                Timber.tag(TAG).e("Unexpected credential class: %s", credential::class.java.name)
                error("Unexpected credential class: ${credential::class.java.name}")
            }
        }
    }
}
