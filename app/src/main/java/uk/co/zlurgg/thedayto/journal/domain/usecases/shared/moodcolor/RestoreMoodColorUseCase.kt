package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorError
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

class RestoreMoodColorUseCase(
    private val repository: MoodColorRepository,
) {
    suspend operator fun invoke(id: Int): EmptyResult<MoodColorError> {
        return when (repository.restore(id)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(MoodColorError.DatabaseError)
        }
    }
}
