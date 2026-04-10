package uk.co.zlurgg.thedayto.auth.data.error

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result

/**
 * Maps authentication exceptions to typed DataError.Auth values.
 * Centralizes auth error handling for Google Sign-In and Firebase auth.
 */
object AuthErrorMapper {

    /**
     * Wraps a suspend auth operation with exception handling.
     * Converts exceptions to typed Result.Error<DataError.Auth>.
     *
     * @param tag Identifier for logging
     * @param action The suspend operation to execute
     * @return Result.Success with the value, or Result.Error with DataError.Auth
     */
    @Suppress("TooGenericExceptionCaught")
    suspend inline fun <T> safeAuthCall(
        tag: String = "AuthErrorMapper",
        action: () -> T,
    ): Result<T, DataError.Auth> {
        return try {
            Result.Success(action())
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            val error = mapException(e)
            Timber.tag(tag).e(e, "Auth operation failed: %s", error)
            Result.Error(error)
        }
    }

    /**
     * Maps an auth-related exception to the appropriate DataError.Auth type.
     */
    fun mapException(e: Exception): DataError.Auth {
        return when (e) {
            is GetCredentialCancellationException -> DataError.Auth.CANCELLED
            is NoCredentialException -> DataError.Auth.NO_CREDENTIAL
            is FirebaseAuthRecentLoginRequiredException -> DataError.Auth.REQUIRES_RECENT_LOGIN
            else -> {
                when {
                    e.message?.contains("network", ignoreCase = true) == true ->
                        DataError.Auth.NETWORK_ERROR

                    else -> DataError.Auth.FAILED
                }
            }
        }
    }
}
