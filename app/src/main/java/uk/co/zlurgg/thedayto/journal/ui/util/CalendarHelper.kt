package uk.co.zlurgg.thedayto.journal.ui.util

import uk.co.zlurgg.thedayto.core.data.util.toLocalDate
import uk.co.zlurgg.thedayto.core.data.util.toStorageEpoch
import uk.co.zlurgg.thedayto.core.domain.util.TimeProvider
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Calendar date comparison utilities for UI.
 *
 * Uses [TimeProvider] for current date, enabling testable calendar logic.
 * All epoch parameters are assumed to be storage format (UTC midnight).
 */
class CalendarHelper(private val timeProvider: TimeProvider) {

    /**
     * Checks if the given epoch timestamp represents today's date.
     */
    fun isToday(epochSeconds: Long): Boolean {
        return epochSeconds == timeProvider.todayStorageEpoch()
    }

    /**
     * Checks if the given epoch timestamp represents a past date.
     */
    fun isPast(epochSeconds: Long): Boolean {
        return epochSeconds < timeProvider.todayStorageEpoch()
    }

    /**
     * Checks if the given epoch timestamp represents a future date.
     */
    fun isFuture(epochSeconds: Long): Boolean {
        return epochSeconds > timeProvider.todayStorageEpoch()
    }

    /**
     * Checks if the given epoch timestamp falls within the specified month/year.
     */
    fun isInMonth(epochSeconds: Long, year: Int, month: Int): Boolean {
        val date = epochSeconds.toLocalDate()
        return date.year == year && date.monthValue == month
    }

    /**
     * Calculates the number of months between two dates.
     *
     * @return The number of months (can be negative if end is before start)
     */
    fun calculateMonthsBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.MONTHS.between(start, end)
    }

    /**
     * Converts a day/month/year to storage epoch.
     * Used for calendar day selection.
     */
    fun dayToStorageEpoch(day: Int, month: Int, year: Int): Long {
        return LocalDate.of(year, month, day).toStorageEpoch()
    }
}
