package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

class DeleteMoodColorUseCase(
    private val repository: MoodColorRepository
) {
    suspend operator fun invoke(id: Int) {
        repository.deleteMoodColor(id)
    }
}