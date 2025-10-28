package uk.co.zlurgg.thedayto.journal.domain.usecases.editor

import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

class DeleteMoodColorUseCase(
    private val repository: MoodColorRepository
) {
    suspend operator fun invoke(moodColor: MoodColor) {
        repository.deleteMoodColor(moodColor)
    }
}