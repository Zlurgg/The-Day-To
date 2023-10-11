package com.jbrightman.thedayto.feature_daily_entry.presentation.display_daily_entries

import com.jbrightman.thedayto.feature_daily_entry.domain.model.DailyEntry
import com.jbrightman.thedayto.domain.util.DailyEntryOrder

sealed class EntriesEvent {
    data class Order(val dailyEntryOrder: DailyEntryOrder): EntriesEvent()
    data class DeleteEntry(val entry: DailyEntry): EntriesEvent()
    data object RestoreEntry: EntriesEvent()
    data object ToggleOrderSection: EntriesEvent()
//    data class ChangeYear(val year: Int) : EntriesEvent()
}