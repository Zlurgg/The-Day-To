package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import timber.log.Timber
import io.github.zlurgg.core.domain.result.getOrNull
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.util.InputValidation
import uk.co.zlurgg.thedayto.journal.domain.util.ValidationResult

/**
 * Use case for updating the name of an existing mood color.
 *
 * Validates:
 * - Mood exists in the repository
 * - New name is valid (not blank, within length limits)
 * - New name doesn't conflict with another mood (case-insensitive)
 *
 * Existing journal entries remain linked via ID, so they will display the new name.
 */
class UpdateMoodColorNameUseCase(
    private val repository: MoodColorRepository
) {
    /**
     * Updates the mood name for the given mood color ID.
     *
     * @param id The ID of the mood color to update
     * @param newMood The new mood name
     * @throws InvalidMoodColorException if validation fails or mood not found
     */
    @Throws(InvalidMoodColorException::class)
    suspend operator fun invoke(id: Int, newMood: String) {
        Timber.d("Attempting to update mood name for ID: $id to: $newMood")

        val moodColor = repository.getMoodColorById(id).getOrNull()
            ?: run {
                Timber.w("Mood color not found for ID: $id")
                throw InvalidMoodColorException("Mood not found")
            }

        // Validate and sanitize the new mood name
        val sanitizedMood = when (val result = InputValidation.validateMood(newMood)) {
            is ValidationResult.Invalid -> {
                Timber.w("Invalid mood name: ${result.message}")
                throw InvalidMoodColorException(result.message)
            }
            is ValidationResult.Valid -> result.value
        }

        // Check if the name is actually changing (case-insensitive comparison)
        if (moodColor.mood.trim().equals(sanitizedMood, ignoreCase = true)) {
            // Name is the same (ignoring case), just update for case changes
            if (moodColor.mood == sanitizedMood) {
                Timber.d("Mood name unchanged, skipping update")
                return
            }
            // Allow case changes (e.g., "happy" -> "Happy")
            Timber.d("Updating mood name case: ${moodColor.mood} -> $sanitizedMood")
        } else {
            // Different name - check for duplicates
            val existingMood = repository.getMoodColorByName(sanitizedMood).getOrNull()
            if (existingMood != null && existingMood.id != id) {
                val isDeleted = existingMood.isDeleted
                val message = if (isDeleted) {
                    "A mood with the name \"$sanitizedMood\" already exists (deleted). Restore it instead."
                } else {
                    "A mood with the name \"$sanitizedMood\" already exists."
                }
                Timber.w("Duplicate mood name detected: $sanitizedMood (existing ID: ${existingMood.id})")
                throw InvalidMoodColorException(message)
            }
            Timber.d("Updating mood name: ${moodColor.mood} -> $sanitizedMood")
        }

        repository.updateMoodColor(
            moodColor.copy(mood = sanitizedMood)
        )
        Timber.i("Successfully updated mood name from '${moodColor.mood}' to '$sanitizedMood' (ID: $id)")
    }
}
