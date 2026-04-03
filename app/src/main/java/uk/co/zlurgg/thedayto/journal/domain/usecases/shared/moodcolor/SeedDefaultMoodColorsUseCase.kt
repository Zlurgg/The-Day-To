package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.core.domain.util.DateUtils
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

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
    /**
     * Seed default mood colors on first launch only.
     */
    suspend operator fun invoke() {
        // Only seed if this is the first launch
        if (!preferencesRepository.isFirstLaunch()) {
            return
        }
        seedDefaults()
    }

    /**
     * Force re-seed default mood colors.
     * Called after sign-out to restore defaults.
     * Uses REPLACE strategy so existing seeds are updated, not duplicated.
     *
     * @return Number of successfully seeded mood colors
     * @throws IllegalStateException if seeding fails
     */
    suspend fun reseed(): Int {
        return seedDefaults()
    }

    private suspend fun seedDefaults(): Int {
        val timestamp = DateUtils.getTodayStartEpoch()

        // Default mood colors with psychology-based colors
        // Fixed syncIds ensure no duplicates across reinstalls
        // updatedAt = 0L ensures user edits always win in conflict resolution
        val defaultMoodColors = listOf(
            MoodColor(
                mood = "Happy",
                color = "FFA726",
                dateStamp = timestamp,
                syncId = "seed_happy",
                updatedAt = 0L
            ),
            MoodColor(
                mood = "Sad",
                color = "1565C0",
                dateStamp = timestamp,
                syncId = "seed_sad",
                updatedAt = 0L
            ),
            MoodColor(
                mood = "In Love",
                color = "EC407A",
                dateStamp = timestamp,
                syncId = "seed_in_love",
                updatedAt = 0L
            ),
            MoodColor(
                mood = "Calm",
                color = "66BB6A",
                dateStamp = timestamp,
                syncId = "seed_calm",
                updatedAt = 0L
            ),
            MoodColor(
                mood = "Excited",
                color = "FFEB3B",
                dateStamp = timestamp,
                syncId = "seed_excited",
                updatedAt = 0L
            ),
            MoodColor(
                mood = "Anxious",
                color = "8E24AA",
                dateStamp = timestamp,
                syncId = "seed_anxious",
                updatedAt = 0L
            ),
            MoodColor(
                mood = "Grateful",
                color = "26A69A",
                dateStamp = timestamp,
                syncId = "seed_grateful",
                updatedAt = 0L
            )
        )

        // Insert all default mood colors and track results
        var successCount = 0
        val failures = mutableListOf<String>()

        defaultMoodColors.forEach { moodColor ->
            when (val result = moodColorRepository.insertMoodColor(moodColor)) {
                is Result.Success -> successCount++
                is Result.Error -> failures.add("${moodColor.mood}: ${result.error}")
            }
        }

        if (successCount == 0 && failures.isNotEmpty()) {
            throw IllegalStateException("Failed to seed any mood colors: ${failures.joinToString()}")
        }

        return successCount
        // Note: First launch is marked complete in OverviewViewModel after tutorial is shown
    }
}
