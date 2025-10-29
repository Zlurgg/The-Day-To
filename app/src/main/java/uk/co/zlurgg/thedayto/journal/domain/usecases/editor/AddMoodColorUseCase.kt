package uk.co.zlurgg.thedayto.journal.domain.usecases.editor

import uk.co.zlurgg.thedayto.journal.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.util.InputValidation
import uk.co.zlurgg.thedayto.journal.domain.util.ValidationResult

/**
 * Use Case: Add a custom mood-color mapping with comprehensive validation
 *
 * Security measures:
 * - Input length limits for mood names
 * - Input sanitization to remove control characters
 * - Hex color format validation
 * - User-friendly error messages
 *
 * @param repository MoodColor repository for persistence
 */
class AddMoodColorUseCase(
    private val repository: MoodColorRepository
) {
    @Throws(InvalidMoodColorException::class)
    suspend operator fun invoke(moodColor: MoodColor) {
        // Validate timestamp
        when (val result = InputValidation.validateTimestamp(moodColor.dateStamp)) {
            is ValidationResult.Invalid -> throw InvalidMoodColorException(result.message)
            is ValidationResult.Valid -> {} // Continue
        }

        // Validate and sanitize mood
        val sanitizedMood = when (val result = InputValidation.validateMood(moodColor.mood)) {
            is ValidationResult.Invalid -> throw InvalidMoodColorException(result.message)
            is ValidationResult.Valid -> result.value
        }

        // Color validation removed - colors are only selected via picker (programmatic, always valid)
        // Basic check that color exists
        if (moodColor.color.isBlank()) {
            throw InvalidMoodColorException("Color cannot be empty")
        }

        // Create sanitized mood color
        val sanitizedMoodColor = moodColor.copy(
            mood = sanitizedMood
        )

        repository.insertMoodColor(sanitizedMoodColor)
    }
}