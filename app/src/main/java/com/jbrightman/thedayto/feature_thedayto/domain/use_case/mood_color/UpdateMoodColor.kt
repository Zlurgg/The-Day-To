package com.jbrightman.thedayto.feature_thedayto.domain.use_case.mood_color

import com.jbrightman.thedayto.feature_thedayto.domain.model.mood_color.MoodColor
import com.jbrightman.thedayto.feature_thedayto.domain.repository.mood_color.MoodColorRepository

class UpdateMoodColor(
    private val repository: MoodColorRepository
) {
    suspend operator fun invoke(moodColor: MoodColor) {
        return repository.updateMoodColor(moodColor)
    }
}