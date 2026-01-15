package io.github.zlurgg.core.domain.error

import io.github.zlurgg.core.domain.result.Result
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Maps exceptions to typed DataError values.
 * Provides safe wrappers for operations that may throw.
 */
object ErrorMapper {

    /**
     * Maps an exception to the appropriate DataError type.
     */
    fun mapException(exception: Exception): DataError {
        return when (exception) {
            // Network errors
            is UnresolvedAddressException -> DataError.Remote.NO_INTERNET
            is UnknownHostException -> DataError.Remote.NO_INTERNET
            is SocketTimeoutException -> DataError.Remote.REQUEST_TIMEOUT
            is IOException -> DataError.Remote.UNKNOWN

            // Database/Storage errors
            is IllegalStateException -> DataError.Local.DATABASE_ERROR
            is IllegalArgumentException -> DataError.Local.UNKNOWN

            // Fallback
            else -> DataError.Local.UNKNOWN
        }
    }

    /**
     * Wraps a suspend operation with exception handling.
     * Converts exceptions to typed Result.Error.
     *
     * @param tag Identifier for logging
     * @param action The suspend operation to execute
     */
    @Suppress("TooGenericExceptionCaught")
    suspend inline fun <T> safeSuspendCall(
        tag: String = "ErrorMapper",
        action: () -> T
    ): Result<T, DataError.Local> {
        return try {
            Result.Success(action())
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            val error = mapException(e) as? DataError.Local ?: DataError.Local.UNKNOWN
            Timber.tag(tag).e(e, "Operation failed: %s", error)
            Result.Error(error)
        }
    }
}
