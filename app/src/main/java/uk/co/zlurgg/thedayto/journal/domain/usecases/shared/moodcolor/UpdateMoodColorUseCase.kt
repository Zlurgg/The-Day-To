package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import uk.co.zlurgg.thedayto.journal.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

class UpdateMoodColorUseCase(
    private val repository: MoodColorRepository
) {
    @Throws(InvalidMoodColorException::class)
    suspend operator fun invoke(id: Int, newColor: String) {
        val moodColor = repository.getMoodColorById(id)
            ?: throw InvalidMoodColorException("Mood not found")

        if (newColor.isBlank()) {
            throw InvalidMoodColorException("Color cannot be empty")
        }

        repository.updateMoodColor(
            moodColor.copy(color = newColor)
        )
    }
}