package uk.co.zlurgg.thedayto.journal.domain.util

/**
 * Input validation constants and utilities for journal entries
 *
 * Provides security constraints to protect against:
 * - DoS attacks via excessive input length
 * - Memory exhaustion
 * - Database bloat
 * - UI breaking with very long strings
 * - Control character injection
 */
object InputValidation {
    /**
     * Maximum length for mood names
     * Rationale: Typical mood descriptors are 1-2 words (e.g., "Happy", "Very Sad")
     */
    const val MAX_MOOD_LENGTH = 50

    /**
     * Maximum length for journal entry content (notes)
     * Rationale: Reasonable limit for daily journaling (roughly 2-3 paragraphs)
     * Average word is ~5 chars, so 5000 chars ≈ 1000 words
     */
    const val MAX_CONTENT_LENGTH = 5000


    /**
     * Sanitize text input by removing control characters and trimming whitespace
     *
     * Security measures:
     * - Removes non-printable control characters (0x00-0x1F, 0x7F-0x9F)
     * - Preserves newlines and tabs for content fields
     * - Trims leading/trailing whitespace
     *
     * @param input Raw user input
     * @param preserveNewlines Whether to keep newline characters (for content fields)
     * @return Sanitized string
     */
    fun sanitizeText(input: String, preserveNewlines: Boolean = false): String {
        val sanitized = if (preserveNewlines) {
            // Keep newlines (\n) and tabs (\t), remove other control chars
            input.replace(Regex("[\u0000-\u0008\u000B-\u000C\u000E-\u001F\u007F-\u009F]"), "")
        } else {
            // Remove all control characters including newlines
            input.replace(Regex("[\u0000-\u001F\u007F-\u009F]"), "")
        }
        return sanitized.trim()
    }

    /**
     * Validate mood name input
     *
     * @param mood User-provided mood name
     * @return ValidationResult with sanitized mood or error message
     */
    fun validateMood(mood: String): ValidationResult {
        val sanitized = sanitizeText(mood, preserveNewlines = false)

        return when {
            sanitized.isBlank() -> ValidationResult.Invalid("Mood cannot be empty")
            sanitized.length > MAX_MOOD_LENGTH -> ValidationResult.Invalid(
                "Mood name is too long (max $MAX_MOOD_LENGTH characters)"
            )
            else -> ValidationResult.Valid(sanitized)
        }
    }

    /**
     * Validate journal entry content (notes)
     *
     * @param content User-provided notes/content
     * @return ValidationResult with sanitized content or error message
     */
    fun validateContent(content: String): ValidationResult {
        val sanitized = sanitizeText(content, preserveNewlines = true)

        return when {
            // Content can be empty (optional field)
            sanitized.length > MAX_CONTENT_LENGTH -> ValidationResult.Invalid(
                "Note is too long (max $MAX_CONTENT_LENGTH characters)"
            )
            else -> ValidationResult.Valid(sanitized)
        }
    }

    /**
     * Validate timestamp
     *
     * @param timestamp Unix epoch timestamp
     * @return ValidationResult with timestamp or error message
     */
    fun validateTimestamp(timestamp: Long): ValidationResult {
        return when {
            timestamp <= 0L -> ValidationResult.Invalid("Invalid date")
            // Future dates are allowed (user might want to pre-fill future entries)
            else -> ValidationResult.Valid(timestamp.toString())
        }
    }
}

/**
 * Sealed class representing validation results
 */
sealed class ValidationResult {
    /**
     * Validation passed
     * @param value The sanitized/validated value
     */
    data class Valid(val value: String) : ValidationResult()

    /**
     * Validation failed
     * @param message User-friendly error message
     */
    data class Invalid(val message: String) : ValidationResult()
}
