package uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case

import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.MoodColor
import uk.co.zlurgg.thedayto.feature_mood_color.domain.repository.MoodColorRepository

class GetMoodColor(
    private val repository: MoodColorRepository
) {
    suspend operator fun invoke(id: Int): MoodColor? {
        return repository.getMoodColorById(id)
    }
}