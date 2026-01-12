package uk.co.zlurgg.thedayto.core.domain.error

/**
 * Typed error hierarchy for The-Day-To.
 * Provides specific error types for different failure scenarios.
 */
sealed interface DataError : Error {

    /**
     * Remote/network errors (GitHub update API).
     */
    enum class Remote : DataError {
        REQUEST_TIMEOUT,
        NO_INTERNET,
        SERVER_ERROR,
        NOT_FOUND,
        UNKNOWN
    }

    /**
     * Local database and storage errors.
     */
    enum class Local : DataError {
        DATABASE_ERROR,
        NOT_FOUND,
        DUPLICATE_ENTRY,
        UNKNOWN
    }

    /**
     * Input validation errors.
     */
    enum class Validation : DataError {
        EMPTY_MOOD,
        EMPTY_COLOR,
        CONTENT_TOO_LONG,
        INVALID_DATE,
        MOOD_NAME_EXISTS,
        MOOD_NOT_FOUND,
        MOOD_DELETED
    }

    /**
     * Authentication errors (Google Sign-In).
     */
    enum class Auth : DataError {
        CANCELLED,
        NO_CREDENTIAL,
        FAILED,
        NETWORK_ERROR
    }
}