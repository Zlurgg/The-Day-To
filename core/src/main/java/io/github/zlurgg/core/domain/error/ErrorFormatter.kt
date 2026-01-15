package io.github.zlurgg.core.domain.error

/**
 * Formats DataError into user-friendly messages.
 */
object ErrorFormatter {

    /**
     * Format a DataError into a user-friendly error message.
     *
     * @param error The DataError to format
     * @param operation Optional operation description (e.g., "save entry")
     * @return Formatted error message
     */
    fun format(error: DataError, operation: String = ""): String {
        val message = when (error) {
            is DataError.Remote -> formatRemoteError(error)
            is DataError.Local -> formatLocalError(error)
            is DataError.Validation -> formatValidationError(error)
            is DataError.Auth -> formatAuthError(error)
        }

        return if (operation.isNotBlank()) {
            "Failed to $operation: $message"
        } else {
            message
        }
    }

    private fun formatRemoteError(error: DataError.Remote): String = when (error) {
        DataError.Remote.REQUEST_TIMEOUT -> "Request timed out. Please try again."
        DataError.Remote.NO_INTERNET -> "No internet connection."
        DataError.Remote.SERVER_ERROR -> "Server error. Please try again later."
        DataError.Remote.NOT_FOUND -> "Not found."
        DataError.Remote.UNKNOWN -> "Network error occurred."
    }

    private fun formatLocalError(error: DataError.Local): String = when (error) {
        DataError.Local.DATABASE_ERROR -> "Database error occurred."
        DataError.Local.NOT_FOUND -> "Item not found."
        DataError.Local.DUPLICATE_ENTRY -> "Entry already exists for this date."
        DataError.Local.UNKNOWN -> "An error occurred."
    }

    private fun formatValidationError(error: DataError.Validation): String = when (error) {
        DataError.Validation.EMPTY_MOOD -> "Please select a mood."
        DataError.Validation.EMPTY_COLOR -> "Please select a color."
        DataError.Validation.CONTENT_TOO_LONG -> "Note is too long."
        DataError.Validation.INVALID_DATE -> "Invalid date selected."
        DataError.Validation.MOOD_NAME_EXISTS -> "A mood with this name already exists."
        DataError.Validation.MOOD_NOT_FOUND -> "Selected mood no longer exists."
        DataError.Validation.MOOD_DELETED -> "Selected mood has been deleted."
    }

    private fun formatAuthError(error: DataError.Auth): String = when (error) {
        DataError.Auth.CANCELLED -> "Sign-in was cancelled."
        DataError.Auth.NO_CREDENTIAL -> "No Google account found on device."
        DataError.Auth.FAILED -> "Sign-in failed. Please try again."
        DataError.Auth.NETWORK_ERROR -> "Network error during sign-in."
    }
}
