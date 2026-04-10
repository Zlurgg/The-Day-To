package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorError
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.util.InputValidation
import uk.co.zlurgg.thedayto.journal.domain.util.MoodColorValidation

class ValidateMoodColorUseCase(
    private val repository: MoodColorRepository,
) {
    suspend operator fun invoke(
        mood: String,
        color: String,
        excludeId: Int? = null,
    ): Result<Unit, MoodColorError> {
        if (mood.isBlank()) {
            return Result.Error(MoodColorError.BlankName)
        }

        if (mood.length > InputValidation.MAX_MOOD_LENGTH) {
            return Result.Error(MoodColorError.NameTooLong)
        }

        if (!color.matches(MoodColorValidation.HEX_COLOR_REGEX)) {
            return Result.Error(MoodColorError.InvalidColor)
        }

        // getMoodColorByName already normalizes (trims + lowercase) internally
        val existing = repository.getMoodColorByName(mood)
        return when {
            existing is Result.Error -> Result.Error(MoodColorError.DatabaseError)
            existing is Result.Success && existing.data != null && existing.data.id != excludeId ->
                Result.Error(MoodColorError.DuplicateName)

            else -> Result.Success(Unit)
        }
    }
}
