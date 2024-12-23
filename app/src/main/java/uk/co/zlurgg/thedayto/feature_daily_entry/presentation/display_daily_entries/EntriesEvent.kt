package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries

import uk.co.zlurgg.thedayto.core.domain.util.DailyEntryOrder
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry

sealed class EntriesEvent {
    data class Order(val dailyEntryOrder: DailyEntryOrder) : EntriesEvent()
    data class DeleteEntry(val entry: DailyEntry) : EntriesEvent()
    data object RestoreEntry : EntriesEvent()
    data object ToggleOrderSection : EntriesEvent()
//    data class ChangeYear(val year: Int) : EntriesEvent()
}