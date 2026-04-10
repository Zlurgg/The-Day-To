package uk.co.zlurgg.thedayto.auth.data.repository

import android.content.Context
import uk.co.zlurgg.thedayto.auth.data.service.GoogleAuthUiClient
import uk.co.zlurgg.thedayto.auth.domain.model.CredentialProvider
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result

/**
 * Implementation of AuthRepository using GoogleAuthUiClient.
 *
 * This adapter:
 * - Receives credentials via callback (no Activity dependency)
 * - Delegates Firebase sign-in to GoogleAuthUiClient
 * - Keeps domain layer clean and framework-independent
 *
 * @param context Application context for GoogleAuthUiClient
 */
class AuthRepositoryImpl(
    context: Context,
) : AuthRepository {

    private val googleAuthUiClient = GoogleAuthUiClient(context)

    override suspend fun signIn(
        credentialProvider: CredentialProvider,
    ): Result<UserData, DataError.Auth> {
        return when (val credentialResult = credentialProvider()) {
            is Result.Success -> {
                googleAuthUiClient.signInWithCredential(credentialResult.data.idToken)
            }

            is Result.Error -> {
                Result.Error(credentialResult.error)
            }
        }
    }

    override suspend fun signOut(): EmptyResult<DataError.Auth> {
        return googleAuthUiClient.signOut()
    }

    override fun getSignedInUser(): UserData? {
        return googleAuthUiClient.getSignedInUser()
    }

    override suspend fun deleteAccount(): EmptyResult<DataError.Auth> {
        return googleAuthUiClient.deleteAccount()
    }

    override suspend fun reauthenticate(credentialProvider: CredentialProvider): EmptyResult<DataError.Auth> {
        return when (val credentialResult = credentialProvider()) {
            is Result.Success -> {
                googleAuthUiClient.reauthenticateWithCredential(credentialResult.data.idToken)
            }

            is Result.Error -> {
                Result.Error(credentialResult.error)
            }
        }
    }
}
