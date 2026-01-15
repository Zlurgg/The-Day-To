package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import io.github.zlurgg.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

class GetEntriesUseCase(
    private val repository: EntryRepository
) {
    operator fun invoke(
        entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending)
    ): Flow<List<EntryWithMoodColor>> {
        return repository.getEntriesWithMoodColors().map { entries ->
            when (entryOrder.orderType) {
                is OrderType.Ascending -> {
                    when (entryOrder) {
                        is EntryOrder.Date -> entries.sortedBy { it.dateStamp }
                        is EntryOrder.Mood -> entries.sortedBy { it.moodName.lowercase() }
                    }
                }

                is OrderType.Descending -> {
                    when (entryOrder) {
                        is EntryOrder.Date -> entries.sortedByDescending { it.dateStamp }
                        is EntryOrder.Mood -> entries.sortedByDescending { it.moodName.lowercase() }
                    }
                }
            }
        }
    }
}