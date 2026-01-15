package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

class GetMoodColorUseCase(
    private val repository: MoodColorRepository
) {
    suspend operator fun invoke(id: Int): Result<MoodColor?, DataError.Local> {
        return repository.getMoodColorById(id)
    }
}