package uk.co.zlurgg.thedayto.journal.domain.usecases.stats

import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor

/**
 * Calculates mood distribution from entries, sorted by frequency
 */
class CalculateMoodDistributionUseCase {

    operator fun invoke(entries: List<EntryWithMoodColor>, limit: Int = 5): List<MoodDistribution> {
        return entries
            .groupBy { it.moodName }
            .map { (mood, moodEntries) ->
                MoodDistribution(
                    mood = mood,
                    color = moodEntries.first().moodColor,
                    count = moodEntries.size
                )
            }
            .sortedByDescending { it.count }
            .take(limit)
    }

    data class MoodDistribution(
        val mood: String,
        val color: String,
        val count: Int
    )
}
