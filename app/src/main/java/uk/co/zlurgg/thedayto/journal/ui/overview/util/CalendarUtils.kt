package uk.co.zlurgg.thedayto.journal.ui.overview.util

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Utility functions for calendar date calculations and comparisons.
 * Extracted from UI layer to follow clean architecture principles.
 */
object CalendarUtils {
    /**
     * Calculates the number of months between two dates.
     *
     * @param start The starting date
     * @param end The ending date
     * @return The number of months between the dates (can be negative if end is before start)
     */
    fun calculateMonthsBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.MONTHS.between(start, end)
    }

    /**
     * Checks if the given epoch timestamp represents today's date.
     *
     * @param epochSeconds The timestamp in epoch seconds (UTC)
     * @param currentDate The date to compare against (typically LocalDate.now())
     * @return true if the epoch timestamp is today, false otherwise
     */
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
     */
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
     */
    fun isFuture(epochSeconds: Long, currentDate: LocalDate): Boolean {
        val currentDateEpoch = currentDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        return epochSeconds > currentDateEpoch
    }
}
