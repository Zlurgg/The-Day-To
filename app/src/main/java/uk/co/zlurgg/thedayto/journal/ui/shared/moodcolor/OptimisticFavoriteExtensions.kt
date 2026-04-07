package uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor

import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorWithCount
import uk.co.zlurgg.thedayto.journal.domain.model.sortedByFavoriteAndUsage

/**
 * Extension to apply optimistic favorite update.
 * Re-sorts list after update to maintain correct order.
 */
fun List<MoodColorWithCount>.withOptimisticFavorite(
    id: Int,
    newValue: Boolean
): List<MoodColorWithCount> = map { mc ->
    if (mc.moodColor.id == id) {
        mc.copy(moodColor = mc.moodColor.copy(isFavorite = newValue))
    } else {
        mc
    }
}.sortedByFavoriteAndUsage()

/**
 * Extension to revert optimistic update using tracked original value.
 */
fun List<MoodColorWithCount>.revertOptimisticFavorite(
    id: Int,
    originalValue: Boolean
): List<MoodColorWithCount> = map { mc ->
    if (mc.moodColor.id == id) {
        mc.copy(moodColor = mc.moodColor.copy(isFavorite = originalValue))
    } else {
        mc
    }
}.sortedByFavoriteAndUsage()
