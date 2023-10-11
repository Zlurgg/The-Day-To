package com.jbrightman.thedayto.feature_mood_color.domain.use_case

import com.jbrightman.thedayto.feature_mood_color.domain.model.MoodColor
import com.jbrightman.thedayto.feature_mood_color.domain.repository.MoodColorRepository

class GetMoodColor(
    private val repository: MoodColorRepository
) {
    suspend operator fun invoke(id: Int): MoodColor? {
        return repository.getMoodColorById(id)
    }
}