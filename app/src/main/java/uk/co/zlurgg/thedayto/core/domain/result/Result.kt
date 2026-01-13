package uk.co.zlurgg.thedayto.core.domain.result

import uk.co.zlurgg.thedayto.core.domain.error.Error

/**
 * A discriminated union that encapsulates a successful outcome with a value of type [D]
 * or a failure with an error of type [E].
 */
sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : uk.co.zlurgg.thedayto.core.domain.error.Error>(val error: E) :
        Result<Nothing, E>
}

/**
 * Maps the success value to a new type.
 */
inline fun <T, E : Error, R> Result<T, E>.map(transform: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(transform(data))
    }
}

/**
 * Chains another Result-returning operation.
 */
inline fun <T, E : Error, R> Result<T, E>.flatMap(transform: (T) -> Result<R, E>): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> transform(data)
    }
}

/**
 * Performs an action on success and returns the original Result.
 */
inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Performs an action on error and returns the original Result.
 */
inline fun <T, E : Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    if (this is Result.Error) action(error)
    return this
}

/**
 * Returns the success value or null if this is an error.
 */
fun <T, E : Error> Result<T, E>.getOrNull(): T? {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> null
    }
}

/**
 * Returns the success value or a default if this is an error.
 */
fun <T, E : Error> Result<T, E>.getOrDefault(defaultValue: T): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> defaultValue
    }
}

/**
 * Returns the success value or computes a value from the error.
 */
inline fun <T, E : Error> Result<T, E>.getOrElse(onError: (E) -> T): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> onError(error)
    }
}

/**
 * Transforms both success and error cases into a single type.
 */
inline fun <T, E : Error, R> Result<T, E>.fold(
    onSuccess: (T) -> R,
    onError: (E) -> R
): R {
    return when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Error -> onError(error)
    }
}

/**
 * Type alias for Result with Unit success (used for operations with no return value).
 */
typealias EmptyResult<E> = Result<Unit, E>

/**
 * Converts a Result<T, E> to EmptyResult<E>, discarding the success value.
 */
fun <T, E : Error> Result<T, E>.asEmptyResult(): EmptyResult<E> {
    return map { }
}
