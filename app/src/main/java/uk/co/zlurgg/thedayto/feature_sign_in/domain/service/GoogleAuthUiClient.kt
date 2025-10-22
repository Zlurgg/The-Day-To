package uk.co.zlurgg.thedayto.feature_sign_in.domain.service

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.feature_sign_in.domain.model.SignInResult
import uk.co.zlurgg.thedayto.feature_sign_in.domain.model.UserData
import java.util.concurrent.CancellationException

/**
 * Modern Google Auth client using Credential Manager API
 *
 * This replaces the deprecated BeginSignInRequest/SignInClient approach
 * with the new Credential Manager API introduced in Android 14+
 */
class GoogleAuthUiClient(
    private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    /**
     * Initiates Google Sign-In flow using Credential Manager
     *
     * @param activityContext The activity context (required for credential UI)
     * @return SignInResult with user data or error message
     */
    suspend fun signIn(activityContext: Context): SignInResult {
        return try {
            // Build Google ID option
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.web_client_id))
                .setAutoSelectEnabled(true)
                .build()

            // Build credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Get credential from Credential Manager
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            // Handle the credential response
            handleSignInResult(result)
        } catch (e: Exception) {
            Timber.e(e, "Error during Google sign-in")
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message ?: "Sign-in failed"
            )
        }
    }

    /**
     * Handles the credential response from Credential Manager
     */
    private suspend fun handleSignInResult(result: GetCredentialResponse): SignInResult {
        return try {
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        // Extract Google ID Token
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        // Sign in to Firebase with the ID token
                        val googleCredentials = GoogleAuthProvider.getCredential(
                            googleIdTokenCredential.idToken,
                            null
                        )

                        val user = auth.signInWithCredential(googleCredentials).await().user

                        SignInResult(
                            data = user?.run {
                                UserData(
                                    userId = uid,
                                    username = displayName,
                                    profilePictureUrl = photoUrl?.toString()
                                )
                            },
                            errorMessage = null
                        )
                    } else {
                        Timber.e("Unexpected credential type: ${credential.type}")
                        SignInResult(
                            data = null,
                            errorMessage = "Unexpected credential type"
                        )
                    }
                }
                else -> {
                    Timber.e("Unexpected credential class: ${credential::class.java.name}")
                    SignInResult(
                        data = null,
                        errorMessage = "Unexpected credential type"
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling sign-in result")
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message ?: "Failed to process sign-in"
            )
        }
    }

    /**
     * Signs out the current user from both Firebase and Google
     */
    suspend fun signOut() {
        try {
            // Clear credential state
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
            // Sign out from Firebase
            auth.signOut()
            Timber.d("User signed out successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error signing out")
            if (e is CancellationException) throw e
        }
    }

    /**
     * Gets the currently signed-in user, if any
     */
    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }
}