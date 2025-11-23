package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder

/**
 * Use case to get entries for a specific month and year.
 *
 * Filters entries at the database level for optimal performance,
 * then applies sorting based on the specified EntryOrder.
 *
 * This approach moves filtering logic from the UI layer to the domain layer,
 * following Clean Architecture principles and improving performance by:
 * - Reducing database query size (only one month of data)
 * - Reducing memory usage in ViewModel (30-31 entries vs potentially thousands)
 * - Eliminating UI-layer filtering computation
 * - Scaling efficiently as users accumulate entries over time
 */
class GetEntriesForMonthUseCase(
    private val repository: EntryRepository
) {
    /**
     * Get entries for a specific month with optional sorting.
     *
     * @param month Month value (1-12)
     * @param year Year value (e.g., 2024)
     * @param entryOrder How to sort the entries (default: Date Descending)
     * @return Flow of sorted entries for the specified month
     * @throws IllegalArgumentException if month is not in 1..12 or year is not positive
     */
    operator fun invoke(
        month: Int,
        year: Int,
        entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending)
    ): Flow<List<EntryWithMoodColor>> {
        return repository.getEntriesForMonth(month, year).map { entries ->
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
