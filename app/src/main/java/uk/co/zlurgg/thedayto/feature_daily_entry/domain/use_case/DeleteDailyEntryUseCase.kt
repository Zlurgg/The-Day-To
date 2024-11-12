package uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case

import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.repository.DailyEntryRepository

class DeleteDailyEntryUseCase(
    private val repository: DailyEntryRepository
) {
    suspend operator fun invoke(entry: DailyEntry) {
        repository.deleteDailyEntry(entry)
    }
}
