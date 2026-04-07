package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorError
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

class DeleteMoodColorUseCase(
    private val repository: MoodColorRepository
) {
    /**
     * Soft-deletes a mood color and returns it for undo support.
     */
    suspend operator fun invoke(id: Int): Result<MoodColor, MoodColorError> {
        // Get the item first (for undo)
        val getResult = repository.getMoodColorById(id)
        val moodColor = when (getResult) {
            is Result.Success -> getResult.data ?: return Result.Error(MoodColorError.NotFound)
            is Result.Error -> return Result.Error(MoodColorError.DatabaseError)
        }

        // Soft delete
        val deleteResult = repository.deleteMoodColor(id)
        if (deleteResult is Result.Error) {
            return Result.Error(MoodColorError.DatabaseError)
        }

        // Return deleted item for undo
        return Result.Success(moodColor)
    }
}
