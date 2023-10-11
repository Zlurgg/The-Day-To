package com.jbrightman.thedayto.feature_daily_entry.presentation.display_daily_entries

import com.jbrightman.thedayto.feature_daily_entry.domain.model.DailyEntry
import com.jbrightman.thedayto.domain.util.DailyEntryOrder
import com.jbrightman.thedayto.domain.util.OrderType

data class EntriesState(
    val entries: List<DailyEntry> = emptyList(),
    val dailyEntryOrder: DailyEntryOrder = DailyEntryOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false
)