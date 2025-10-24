package uk.co.zlurgg.thedayto.journal.domain.usecases.entry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

class GetEntriesUseCase(
    private val repository: EntryRepository
) {
    operator fun invoke(
        entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending)
    ): Flow<List<Entry>> {
        return repository.getEntries().map { entries ->
            when (entryOrder.orderType) {
                is OrderType.Ascending -> {
                    when (entryOrder) {
                        is EntryOrder.Date -> entries.sortedBy { it.dateStamp }
                        is EntryOrder.Mood -> entries.sortedBy { it.mood.lowercase() }
                    }
                }

                is OrderType.Descending -> {
                    when (entryOrder) {
                        is EntryOrder.Date -> entries.sortedByDescending { it.dateStamp }
                        is EntryOrder.Mood -> entries.sortedByDescending { it.mood.lowercase() }
                    }
                }
            }
        }
    }
}