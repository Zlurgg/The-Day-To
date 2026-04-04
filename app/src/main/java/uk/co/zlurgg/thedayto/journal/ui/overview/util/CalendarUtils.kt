package uk.co.zlurgg.thedayto.journal.ui.overview.util

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Utility functions for calendar date calculations and comparisons.
 *
 * @deprecated Use [CalendarHelper] instead. This object will be removed in a future release.
 */
@Deprecated("Use CalendarHelper instead")
object CalendarUtils {
    /**
     * Calculates the number of months between two dates.
     *
     * @param start The starting date
     * @param end The ending date
     * @return The number of months between the dates (can be negative if end is before start)
     *
     * @deprecated Use CalendarHelper.calculateMonthsBetween()
     */
    @Deprecated(
        message = "Use CalendarHelper.calculateMonthsBetween()",
        replaceWith = ReplaceWith(
            expression = "calendarHelper.calculateMonthsBetween(start, end)",
            imports = ["uk.co.zlurgg.thedayto.journal.ui.util.CalendarHelper"]
        )
    )
    fun calculateMonthsBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.MONTHS.between(start, end)
    }

    /**
     * Checks if the given epoch timestamp represents today's date.
     *
     * @param epochSeconds The timestamp in epoch seconds (UTC)
     * @param currentDate The date to compare against (typically LocalDate.now())
     * @return true if the epoch timestamp is today, false otherwise
     *
     * @deprecated Use CalendarHelper.isToday() which gets current date from TimeProvider
     */
    @Deprecated(
        message = "Use CalendarHelper.isToday(epochSeconds)",
        replaceWith = ReplaceWith(
            expression = "calendarHelper.isToday(epochSeconds)",
            imports = ["uk.co.zlurgg.thedayto.journal.ui.util.CalendarHelper"]
        )
    )
    fun isToday(epochSeconds: Long, currentDate: LocalDate): Boolean {
        val currentDateEpoch = currentDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        return epochSeconds == currentDateEpoch
    }

    /**
     * Checks if the given epoch timestamp represents a past date.
     *
     * @param epochSeconds The timestamp in epoch seconds (UTC)
     * @param currentDate The date to compare against (typically LocalDate.now())
     * @return true if the epoch timestamp is in the past, false otherwise
     *
     * @deprecated Use CalendarHelper.isPast() which gets current date from TimeProvider
     */
    @Deprecated(
        message = "Use CalendarHelper.isPast(epochSeconds)",
        replaceWith = ReplaceWith(
            expression = "calendarHelper.isPast(epochSeconds)",
            imports = ["uk.co.zlurgg.thedayto.journal.ui.util.CalendarHelper"]
        )
    )
    fun isPast(epochSeconds: Long, currentDate: LocalDate): Boolean {
        val currentDateEpoch = currentDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        return epochSeconds < currentDateEpoch
    }

    /**
     * Checks if the given epoch timestamp represents a future date.
     *
     * @param epochSeconds The timestamp in epoch seconds (UTC)
     * @param currentDate The date to compare against (typically LocalDate.now())
     * @return true if the epoch timestamp is in the future, false otherwise
     *
     * @deprecated Use CalendarHelper.isFuture() which gets current date from TimeProvider
     */
    @Deprecated(
        message = "Use CalendarHelper.isFuture(epochSeconds)",
        replaceWith = ReplaceWith(
            expression = "calendarHelper.isFuture(epochSeconds)",
            imports = ["uk.co.zlurgg.thedayto.journal.ui.util.CalendarHelper"]
        )
    )
    fun isFuture(epochSeconds: Long, currentDate: LocalDate): Boolean {
        val currentDateEpoch = currentDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        return epochSeconds > currentDateEpoch
    }
}
