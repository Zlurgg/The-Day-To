package com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries

import com.jbrightman.thedayto.feature_thedayto.domain.model.entry.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.util.EntryOrder
import com.jbrightman.thedayto.feature_thedayto.domain.util.OrderType

data class EntriesState(
    val entries: List<TheDayToEntry> = emptyList(),
    val entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false
)