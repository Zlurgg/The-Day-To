package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.state

import uk.co.zlurgg.thedayto.core.domain.util.DailyEntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry

data class EntriesUiState(
    val entries: List<DailyEntry> = emptyList(),
    val dailyEntryOrder: DailyEntryOrder = DailyEntryOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false,
    val dailyEntryMade: Boolean = false,
    val recentlyDeletedEntry: DailyEntry? = null
)
