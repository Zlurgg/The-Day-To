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
        // Normalize color (strip alpha if 8-char ARGB) and trim whitespace from name
        val normalized = moodColor.copy(
            mood = moodColor.mood.trim(),
            color = MoodColorValidation.normalizeHexColor(moodColor.color)
        )

        // For inserts, the moodNormalized column is UNIQUE in the DB, so a soft-deleted
        // row with the same name has to be restored rather than re-inserted.
        if (normalized.id == null) {
            val restoreOutcome = restoreIfSoftDeleted(normalized)
            if (restoreOutcome != null) return restoreOutcome
        }

        // Validate (rejects blank/long names, invalid colors, active duplicates)
        val validation = validate(normalized.mood, normalized.color, normalized.id)
        if (validation is Result.Error) {
            return Result.Error(validation.error)
        }

        return if (normalized.id == null) {
            insertNew(normalized)
        } else {
            updateExisting(normalized)
        }
    }

    /**
     * Returns a result if a soft-deleted mood with the same name exists (and was restored),
     * or null to indicate the caller should proceed with normal insert validation.
     */
    private suspend fun restoreIfSoftDeleted(
        normalized: MoodColor
    ): Result<MoodColor, MoodColorError>? {
        val existingByName = repository.getMoodColorByName(normalized.mood)
        if (existingByName is Result.Error) {
            return Result.Error(MoodColorError.DatabaseError)
        }
        val existing = (existingByName as Result.Success).data ?: return null
        if (!existing.isDeleted) return null

        val restored = existing.copy(
            color = normalized.color,
            isDeleted = false
        )
        return when (repository.updateMoodColor(restored)) {
            is Result.Success -> Result.Success(restored)
            is Result.Error -> Result.Error(MoodColorError.DatabaseError)
        }
    }

    private suspend fun insertNew(normalized: MoodColor): Result<MoodColor, MoodColorError> {
        val countResult = repository.getActiveCount()
        if (countResult is Result.Success && countResult.data >= MAX_MOOD_COLORS) {
            return Result.Error(MoodColorError.LimitReached)
        }
        return when (val result = repository.insertMoodColor(normalized)) {
            is Result.Success -> Result.Success(normalized.copy(id = result.data.toInt()))
            is Result.Error -> Result.Error(MoodColorError.DatabaseError)
        }
    }

    private suspend fun updateExisting(
        normalized: MoodColor
    ): Result<MoodColor, MoodColorError> {
        // Verify the row still exists; Room's @Update silently no-ops on missing IDs,
        // which would mask deletion races between devices.
        val existingResult = repository.getMoodColorById(requireNotNull(normalized.id))
        if (existingResult is Result.Error) {
            return Result.Error(MoodColorError.DatabaseError)
        }
        if ((existingResult as Result.Success).data == null) {
            return Result.Error(MoodColorError.NotFound)
        }
        return when (repository.updateMoodColor(normalized)) {
            is Result.Success -> Result.Success(normalized)
            is Result.Error -> Result.Error(MoodColorError.DatabaseError)
        }
    }
}
