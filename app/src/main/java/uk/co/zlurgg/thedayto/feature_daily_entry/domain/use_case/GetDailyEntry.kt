package uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case

import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.repository.DailyEntryRepository

class GetDailyEntry(
    private val repository: DailyEntryRepository
) {
    suspend operator fun invoke(id: Int): DailyEntry? {
        return repository.getDailyEntryById(id)
    }
}