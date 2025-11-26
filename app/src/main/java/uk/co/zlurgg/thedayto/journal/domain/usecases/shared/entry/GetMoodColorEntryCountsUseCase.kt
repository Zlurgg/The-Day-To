package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry

import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

/**
 * Use case for getting the count of entries for each mood color.
 * Used by the Mood Color Management screen to show usage statistics.
 */
class GetMoodColorEntryCountsUseCase(
    private val repository: EntryRepository
) {
    /**
     * @return Flow of map (moodColorId to entryCount)
     */
    operator fun invoke(): Flow<Map<Int, Int>> {
        return repository.getMoodColorEntryCounts()
    }
}
