package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorError
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.util.MoodColorValidation

class SaveMoodColorUseCase(
    private val validate: ValidateMoodColorUseCase,
    private val repository: MoodColorRepository
) {
    companion object {
        const val MAX_MOOD_COLORS = 50
    }

    suspend operator fun invoke(moodColor: MoodColor): Result<MoodColor, MoodColorError> {
        // Normalize color (strip alpha if 8-char ARGB from color picker)
        val normalizedColor = MoodColorValidation.normalizeHexColor(moodColor.color)
        // Trim whitespace from mood name
        val normalizedMood = moodColor.mood.trim()
        val normalizedMoodColor = moodColor.copy(
            mood = normalizedMood,
            color = normalizedColor
        )

        // Validate
        val validation = validate(
            normalizedMoodColor.mood,
            normalizedMoodColor.color,
            normalizedMoodColor.id
        )
        if (validation is Result.Error) {
            return Result.Error(validation.error)
        }

        // Insert or update
        return if (normalizedMoodColor.id == null) {
            // Check limit before insert
            val countResult = repository.getActiveCount()
            if (countResult is Result.Success && countResult.data >= MAX_MOOD_COLORS) {
                return Result.Error(MoodColorError.LimitReached)
            }

            when (val result = repository.insertMoodColor(normalizedMoodColor)) {
                is Result.Success -> {
                    // Return the mood color with the new ID
                    Result.Success(normalizedMoodColor.copy(id = result.data.toInt()))
                }
                is Result.Error -> Result.Error(MoodColorError.DatabaseError)
            }
        } else {
            when (val result = repository.updateMoodColor(normalizedMoodColor)) {
                is Result.Success -> Result.Success(normalizedMoodColor)
                is Result.Error -> Result.Error(MoodColorError.DatabaseError)
            }
        }
    }
}
