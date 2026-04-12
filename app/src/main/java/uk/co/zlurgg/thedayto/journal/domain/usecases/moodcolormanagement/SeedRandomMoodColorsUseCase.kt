package uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolormanagement

import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.core.domain.util.TimeProvider
import uk.co.zlurgg.thedayto.journal.domain.model.CuratedMoods
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorError
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SaveMoodColorUseCase
import kotlin.random.Random

/**
 * Picks 5-10 random moods from [CuratedMoods.ALL] that don't already exist
 * in the user's palette and saves each via [SaveMoodColorUseCase].
 *
 * By routing through SaveMoodColorUseCase, the random seeder inherits:
 * - Name validation and normalization
 * - The 50-mood-color cap ([MoodColorError.LimitReached])
 * - Soft-delete restore behaviour
 * - Sync status assignment
 *
 * @return The number of moods successfully added (0 if all curated moods
 *   already exist or the pool is exhausted).
 */
class SeedRandomMoodColorsUseCase(
    private val saveMoodColor: SaveMoodColorUseCase,
    private val repository: MoodColorRepository,
    private val timeProvider: TimeProvider,
) {
    companion object {
        private const val MIN_SEED_COUNT = 5
        private const val MAX_SEED_COUNT = 10
    }

    suspend operator fun invoke(): Result<Int, MoodColorError> {
        // 1. Bulk-read existing mood names (normalized to lowercase by the DAO)
        val existingNames = repository.getActiveMoodNames()

        // 2. Filter curated list to moods not already present
        val available = CuratedMoods.ALL.filter { seed ->
            seed.mood.trim().lowercase() !in existingNames
        }

        if (available.isEmpty()) return Result.Success(0)

        // 3. Pick a random count between MIN and MAX, capped at what's available
        val count = Random.nextInt(MIN_SEED_COUNT, MAX_SEED_COUNT + 1)
            .coerceAtMost(available.size)

        // 4. Shuffle and take
        val selected = available.shuffled().take(count)

        // 5. Save each via SaveMoodColorUseCase (gets validation, 50-cap, sync for free)
        var successCount = 0
        for (seed in selected) {
            val moodColor = MoodColor(
                mood = seed.mood,
                color = seed.color,
                dateStamp = timeProvider.instant().toEpochMilli(),
            )
            when (val result = saveMoodColor(moodColor)) {
                is Result.Success -> successCount++
                is Result.Error -> if (result.error == MoodColorError.LimitReached) {
                    return Result.Success(successCount) // Hit 50 cap, return what we got
                }
                // Other errors (DuplicateName race, DB glitch) — skip, try next
            }
        }

        return Result.Success(successCount)
    }
}
