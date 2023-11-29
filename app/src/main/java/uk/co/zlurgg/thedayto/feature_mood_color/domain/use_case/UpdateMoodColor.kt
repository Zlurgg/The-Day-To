package uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case

import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.MoodColor
import uk.co.zlurgg.thedayto.feature_mood_color.domain.repository.MoodColorRepository

class UpdateMoodColor(
    private val repository: MoodColorRepository
) {
    suspend operator fun invoke(moodColor: MoodColor) {
        return repository.updateMoodColor(moodColor)
    }
}