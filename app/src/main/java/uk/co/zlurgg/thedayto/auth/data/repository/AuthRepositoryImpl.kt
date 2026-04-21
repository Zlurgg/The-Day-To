package uk.co.zlurgg.thedayto.auth.data.repository

import android.content.Context
import uk.co.zlurgg.thedayto.auth.data.service.GoogleAuthUiClient
import uk.co.zlurgg.thedayto.auth.domain.model.IdToken
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result

/**
 * Implementation of AuthRepository using GoogleAuthUiClient.
 *
 * Receives an IdToken directly — no lambda invocation needed.
 * Delegates Firebase sign-in to GoogleAuthUiClient.
 *
 * @param context Application context for GoogleAuthUiClient
 */
class AuthRepositoryImpl(
    context: Context,
) : AuthRepository {

    private val googleAuthUiClient = GoogleAuthUiClient(context)

    override suspend fun signIn(idToken: IdToken): Result<UserData, DataError.Auth> {
        return googleAuthUiClient.signInWithCredential(idToken.value)
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

    override suspend fun reauthenticate(idToken: IdToken): EmptyResult<DataError.Auth> {
        return googleAuthUiClient.reauthenticateWithCredential(idToken.value)
    }
}
