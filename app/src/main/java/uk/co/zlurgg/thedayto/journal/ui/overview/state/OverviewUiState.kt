package uk.co.zlurgg.thedayto.journal.ui.overview.state

import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.domain.model.Entry

data class OverviewUiState(
    val entries: List<Entry> = emptyList(),
    val entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending),
    val entryMade: Boolean = false,
    val recentlyDeletedEntry: Entry? = null,
    val isLoading: Boolean = false
)
