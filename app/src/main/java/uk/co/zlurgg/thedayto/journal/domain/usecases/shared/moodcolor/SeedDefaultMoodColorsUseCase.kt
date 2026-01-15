package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import io.github.zlurgg.core.domain.util.DateUtils
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

/**
 * Use Case: Seed default mood colors on first app launch
 *
 * Provides users with a starter set of mood-color combinations to help them
 * understand and immediately use the journaling feature without manual setup.
 *
 * Default mood colors (based on color psychology):
 * - Happy (Warm Orange) - Energetic, positive
 * - Sad (Deep Blue) - Melancholy, introspective
 * - In Love (Pink) - Romance, affection
 * - Calm (Soft Green) - Peaceful, balanced
 * - Excited (Bright Yellow) - Enthusiastic, joyful
 * - Anxious (Purple) - Stress, worry
 * - Grateful (Teal) - Appreciation, mindfulness
 *
 * @param moodColorRepository Repository for persisting mood colors
 * @param preferencesRepository Repository for tracking first launch state
 */
class SeedDefaultMoodColorsUseCase(
    private val moodColorRepository: MoodColorRepository,
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke() {
        // Only seed if this is the first launch
        if (!preferencesRepository.isFirstLaunch()) {
            return
        }

        val timestamp = DateUtils.getTodayStartEpoch()

        // Default mood colors with psychology-based colors
        val defaultMoodColors = listOf(
            MoodColor(mood = "Happy", color = "FFA726", dateStamp = timestamp),
            MoodColor(mood = "Sad", color = "1565C0", dateStamp = timestamp),
            MoodColor(mood = "In Love", color = "EC407A", dateStamp = timestamp),
            MoodColor(mood = "Calm", color = "66BB6A", dateStamp = timestamp),
            MoodColor(mood = "Excited", color = "FFEB3B", dateStamp = timestamp),
            MoodColor(mood = "Anxious", color = "8E24AA", dateStamp = timestamp),
            MoodColor(mood = "Grateful", color = "26A69A", dateStamp = timestamp)
        )

        // Insert all default mood colors
        defaultMoodColors.forEach { moodColor ->
            moodColorRepository.insertMoodColor(moodColor)
        }

        // Note: First launch is marked complete in OverviewViewModel after tutorial is shown
    }
}
