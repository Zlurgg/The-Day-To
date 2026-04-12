package uk.co.zlurgg.thedayto.journal.domain.model

/**
 * Single source of truth for mood color sorting: favorites pinned at the top,
 * then by usage count descending, then alphabetical as tiebreaker.
 *
 * Used by [GetSortedMoodColorsUseCase] which feeds both the Management screen
 * and the Editor dropdown — so the two lists always match.
 */
fun List<MoodColorWithCount>.sortedByFavoriteAndUsage(): List<MoodColorWithCount> =
    sortedWith(
        compareByDescending<MoodColorWithCount> { it.moodColor.isFavorite }
            .thenByDescending { it.entryCount }
            .thenBy { it.moodColor.mood.lowercase() },
    )
