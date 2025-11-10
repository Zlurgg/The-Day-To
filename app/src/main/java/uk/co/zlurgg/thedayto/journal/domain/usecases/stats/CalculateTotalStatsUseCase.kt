package uk.co.zlurgg.thedayto.journal.domain.usecases.stats

import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Calculates all-time statistics: first entry date and average entries per month
 */
class CalculateTotalStatsUseCase {

    operator fun invoke(entries: List<Entry>): TotalStats {
        if (entries.isEmpty()) {
            return TotalStats(
                firstEntryDate = null,
                averageEntriesPerMonth = 0f
            )
        }

        val firstDate = calculateFirstEntryDate(entries)
        val averagePerMonth = calculateAverageEntriesPerMonth(entries, firstDate)

        return TotalStats(
            firstEntryDate = firstDate,
            averageEntriesPerMonth = averagePerMonth
        )
    }

    private fun calculateFirstEntryDate(entries: List<Entry>): LocalDate? {
        val oldestEntry = entries.minByOrNull { it.dateStamp } ?: return null
        return Instant.ofEpochSecond(oldestEntry.dateStamp)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
    }

    private fun calculateAverageEntriesPerMonth(entries: List<Entry>, firstDate: LocalDate?): Float {
        if (firstDate == null) return 0f

        val now = LocalDate.now()
        val monthsBetween = ChronoUnit.MONTHS.between(firstDate, now) + 1

        return if (monthsBetween > 0) {
            entries.size.toFloat() / monthsBetween
        } else {
            entries.size.toFloat()
        }
    }

    data class TotalStats(
        val firstEntryDate: LocalDate?,
        val averageEntriesPerMonth: Float
    )
}
