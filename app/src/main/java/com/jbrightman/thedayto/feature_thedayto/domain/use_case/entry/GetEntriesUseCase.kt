package com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry

import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.repository.TheDayToRepository
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
                        is EntryOrder.Title -> entries.sortedBy { it.mood.lowercase() }
                        is EntryOrder.Color -> entries.sortedBy { it.content }
                    }
                }
                is OrderType.Descending -> {
                    when (entryOrder) {
                        is EntryOrder.Date -> entries.sortedByDescending { it.dateStamp }
                        is EntryOrder.Title -> entries.sortedByDescending { it.mood.lowercase() }
                        is EntryOrder.Color -> entries.sortedByDescending { it.content }
                    }
                }
            }
        }
    }
}