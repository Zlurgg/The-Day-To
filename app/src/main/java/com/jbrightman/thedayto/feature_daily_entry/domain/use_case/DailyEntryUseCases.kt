package com.jbrightman.thedayto.feature_daily_entry.domain.use_case

data class DailyEntryUseCases(
    val getEntries: GetDailyEntriesUseCase,
    val deleteEntry: DeleteDailyEntryUseCase,
    val addDailyEntry: AddDailyEntry,
    val getDailyEntry: GetDailyEntry,
    val updateDailyEntry: UpdateDailyEntry
)