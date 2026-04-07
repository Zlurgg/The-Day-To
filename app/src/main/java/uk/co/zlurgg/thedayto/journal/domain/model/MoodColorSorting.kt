package uk.co.zlurgg.thedayto.journal.domain.model

/**
 * Single source of truth for mood color sorting.
 * Favorites first, then by entry count descending.
 */
fun List<MoodColorWithCount>.sortedByFavoriteAndUsage(): List<MoodColorWithCount> =
    sortedWith(
        compareByDescending<MoodColorWithCount> { it.moodColor.isFavorite }
            .thenByDescending { it.entryCount }
    )
