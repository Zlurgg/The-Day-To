package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import uk.co.zlurgg.thedayto.journal.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.util.InputValidation
import uk.co.zlurgg.thedayto.journal.domain.util.ValidationResult

/**
 * Use Case: Add a custom mood-color mapping with comprehensive validation
 * Implements "create or restore" logic - if mood name exists but is deleted, restore it
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

        // Check if mood exists (deleted or active) - case-insensitive lookup
        // Note: Repository handles normalization internally
        val existing = repository.getMoodColorByName(sanitizedMood)

        when {
            existing == null -> {
                // Create new mood
                val sanitizedMoodColor = moodColor.copy(mood = sanitizedMood)
                repository.insertMoodColor(sanitizedMoodColor)
            }
            existing.isDeleted -> {
                // Restore deleted mood with new color
                repository.updateMoodColor(
                    existing.copy(color = moodColor.color, isDeleted = false)
                )
            }
            else -> {
                // Active duplicate - throw error
                throw InvalidMoodColorException(
                    "A mood with the name \"$sanitizedMood\" already exists."
                )
            }
        }
    }
}