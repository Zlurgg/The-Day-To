package uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case

import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.MoodColor
import uk.co.zlurgg.thedayto.feature_mood_color.domain.repository.MoodColorRepository

class AddMoodColor(
    private val repository: MoodColorRepository
) {
    @Throws(InvalidMoodColorException::class)
    suspend operator fun invoke(moodColor: MoodColor) {
        if (moodColor.dateStamp == 0L) {
            throw InvalidMoodColorException("The date of the new mood color selection can't be empty.")
        }
        if (moodColor.mood.isBlank()) {
            throw InvalidMoodColorException("The mood of the new mood color selection can't be empty.")
        }
        if (moodColor.color == "") {
            throw InvalidMoodColorException("The color of the new mood color selection can't be empty.")
        }
        repository.insertMoodColor(moodColor)
    }
}