package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorWithCount
import uk.co.zlurgg.thedayto.journal.domain.model.sortedByFavoriteAndUsage
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

class GetSortedMoodColorsUseCase(
    private val moodColorRepository: MoodColorRepository,
    private val entryRepository: EntryRepository
) {
    /**
     * Returns mood colors sorted by: favorites first, then by usage count descending.
     * This is a Flow that automatically updates when data changes.
     */
    operator fun invoke(): Flow<List<MoodColorWithCount>> {
        return combine(
            moodColorRepository.getMoodColors(),
            entryRepository.getMoodColorEntryCounts()
        ) { moodColors, counts ->
            moodColors
                .map { mc -> MoodColorWithCount(mc, counts[mc.id] ?: 0) }
                .sortedByFavoriteAndUsage()
        }
    }
}
