package uk.co.zlurgg.thedayto.auth.data.service

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
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.EmptyResult
import io.github.zlurgg.core.domain.result.Result
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
    private val credentialManager = CredentialManager.Companion.create(context)

    /**
     * Initiates Google Sign-In flow using Credential Manager
     *
     * @param activityContext The activity context (required for credential UI)
     * @return Result with UserData on success or DataError.Auth on failure
     */
    suspend fun signIn(activityContext: Context): Result<UserData, DataError.Auth> {
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

            // Map exception to typed error
            val error = when {
                e.message?.contains("No credentials available", ignoreCase = true) == true ->
                    DataError.Auth.NO_CREDENTIAL
                e.message?.contains("16", ignoreCase = true) == true ->
                    DataError.Auth.FAILED
                e.message?.contains("network", ignoreCase = true) == true ->
                    DataError.Auth.NETWORK_ERROR
                e.message?.contains("cancel", ignoreCase = true) == true ->
                    DataError.Auth.CANCELLED
                else -> DataError.Auth.FAILED
            }

            Result.Error(error)
        }
    }

    /**
     * Handles the credential response from Credential Manager
     */
    private suspend fun handleSignInResult(result: GetCredentialResponse): Result<UserData, DataError.Auth> {
        return try {
            when (val credential = result.credential) {
                is CustomCredential -> handleCustomCredential(credential)
                else -> {
                    Timber.e("Unexpected credential class: ${credential::class.java.name}")
                    Result.Error(DataError.Auth.FAILED)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling sign-in result")
            if (e is CancellationException) throw e
            Result.Error(DataError.Auth.FAILED)
        }
    }

    /**
     * Handles Google ID Token credential and signs in to Firebase
     */
    private suspend fun handleCustomCredential(credential: CustomCredential): Result<UserData, DataError.Auth> {
        if (credential.type != GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            Timber.e("Unexpected credential type: ${credential.type}")
            return Result.Error(DataError.Auth.FAILED)
        }

        // Extract Google ID Token
        val googleIdTokenCredential = GoogleIdTokenCredential.Companion
            .createFrom(credential.data)

        // Sign in to Firebase with the ID token
        val googleCredentials = GoogleAuthProvider.getCredential(
            googleIdTokenCredential.idToken,
            null
        )

        val user = auth.signInWithCredential(googleCredentials).await().user
            ?: return Result.Error(DataError.Auth.FAILED)

        return Result.Success(
            UserData(
                userId = user.uid,
                username = user.displayName,
                profilePictureUrl = user.photoUrl?.toString()
            )
        )
    }

    /**
     * Signs out the current user from both Firebase and Google
     */
    suspend fun signOut(): EmptyResult<DataError.Auth> {
        return try {
            // Clear credential state
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
            // Sign out from Firebase
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