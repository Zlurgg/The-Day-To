package uk.co.zlurgg.thedayto.journal.domain.usecases.stats

import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale

/**
 * Calculates monthly breakdown of entries with completion rates
 */
class CalculateMonthlyBreakdownUseCase {

    operator fun invoke(entries: List<EntryWithMoodColor>, limit: Int = 6): List<MonthlyStats> {
        return entries
            .groupBy { entry ->
                val date = Instant.ofEpochSecond(entry.dateStamp)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                YearMonth.of(date.year, date.month)
            }
            .map { (yearMonth, monthEntries) ->
                val daysInMonth = yearMonth.lengthOfMonth()
                val completionRate = ((monthEntries.size.toFloat() / daysInMonth) * 100).toInt()

                MonthlyStats(
                    month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    year = yearMonth.year,
                    monthValue = yearMonth.monthValue,
                    entryCount = monthEntries.size,
                    completionRate = completionRate,
                )
            }
            .sortedByDescending { it.year * YEAR_MONTH_SORT_MULTIPLIER + it.monthValue }
            .take(limit)
    }

    data class MonthlyStats(
        val month: String,
        val year: Int,
        val monthValue: Int,
        val entryCount: Int,
        val completionRate: Int,
    )

    companion object {
        /** Multiplier for combining year + monthValue into a sortable int (e.g. 2024 * 100 + 3 = 202403). */
        private const val YEAR_MONTH_SORT_MULTIPLIER = 100
    }
}
