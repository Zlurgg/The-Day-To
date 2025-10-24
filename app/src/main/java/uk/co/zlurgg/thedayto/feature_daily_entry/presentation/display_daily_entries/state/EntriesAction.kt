package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.state

import uk.co.zlurgg.thedayto.core.domain.util.DailyEntryOrder
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry

sealed interface EntriesAction {
    data class Order(val dailyEntryOrder: DailyEntryOrder) : EntriesAction
    data class DeleteEntry(val entry: DailyEntry) : EntriesAction
    data object RestoreEntry : EntriesAction
    data object ToggleOrderSection : EntriesAction
}
