package uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case

import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.repository.DailyEntryRepository
import uk.co.zlurgg.thedayto.domain.util.DailyEntryOrder
import uk.co.zlurgg.thedayto.domain.util.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDailyEntriesUseCase(
    private val repository: DailyEntryRepository
) {
    operator fun invoke(
        dailyEntryOrder: DailyEntryOrder = DailyEntryOrder.Date(OrderType.Descending)
    ): Flow<List<DailyEntry>> {
        return repository.getDailyEntries().map { entries ->
            when (dailyEntryOrder.orderType) {
                is OrderType.Ascending -> {
                    when (dailyEntryOrder) {
                        is DailyEntryOrder.Date -> entries.sortedBy { it.dateStamp }
                        is DailyEntryOrder.Mood -> entries.sortedBy { it.mood.lowercase() }
                    }
                }
                is OrderType.Descending -> {
                    when (dailyEntryOrder) {
                        is DailyEntryOrder.Date -> entries.sortedByDescending { it.dateStamp }
                        is DailyEntryOrder.Mood -> entries.sortedByDescending { it.mood.lowercase() }
                    }
                }
            }
        }
    }
}
