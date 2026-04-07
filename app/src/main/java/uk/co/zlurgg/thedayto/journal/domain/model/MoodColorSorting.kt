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

/**
 * Sort mood colors by favorites first (for contexts without entry counts).
 * Used in Editor dropdown where entry counts aren't displayed.
 */
fun List<MoodColor>.sortedByFavorite(): List<MoodColor> =
    sortedByDescending { it.isFavorite }
