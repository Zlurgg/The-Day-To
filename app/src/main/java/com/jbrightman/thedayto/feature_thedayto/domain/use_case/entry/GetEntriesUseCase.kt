package com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry

import com.jbrightman.thedayto.feature_thedayto.domain.model.entry.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.repository.entry.TheDayToRepository
import com.jbrightman.thedayto.feature_thedayto.domain.util.EntryOrder
import com.jbrightman.thedayto.feature_thedayto.domain.util.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetEntriesUseCase(
    private val repository: TheDayToRepository
) {
    operator fun invoke(
        entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending)
    ): Flow<List<TheDayToEntry>> {
        return repository.getTheDayToEntries().map { entries ->
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
