package com.jbrightman.thedayto.feature_daily_entry.domain.use_case

import com.jbrightman.thedayto.feature_daily_entry.domain.model.DailyEntry
import com.jbrightman.thedayto.feature_daily_entry.domain.repository.DailyEntryRepository

class DeleteDailyEntryUseCase (
    private val repository: DailyEntryRepository
) {
    suspend operator fun invoke(entry: DailyEntry) {
        repository.deleteDailyEntry(entry)
    }
}
