package uk.co.zlurgg.thedayto.journal.ui.util

import uk.co.zlurgg.thedayto.core.data.util.toLocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Stateless date formatting utilities for UI display.
 *
 * Uses pure extension functions for epoch conversion - no dependencies needed.
 * All methods convert storage epoch (UTC midnight) to display format.
 */
object DateFormatter {

    /**
     * Formats epoch seconds to display string: "15 Jan 2024"
     */
    fun formatDate(epochSeconds: Long): String {
        val date = epochSeconds.toLocalDate()
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val year = date.year
        return "$day $month $year"
    }

    /**
     * Returns the parts needed to render a date with an ordinal suffix,
     * e.g. "15th January 2024". The suffix is separated so the UI layer
     * can render it as superscript.
     */
    fun formatDateOrdinal(epochSeconds: Long): OrdinalDate {
        val date = epochSeconds.toLocalDate()
        return OrdinalDate(
            day = date.dayOfMonth,
            suffix = ordinalSuffix(date.dayOfMonth),
            month = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
            year = date.year,
        )
    }

    /**
     * English ordinal suffix for a day number: "st", "nd", "rd", or "th".
     */
    private fun ordinalSuffix(day: Int): String = when {
        day in 11..13 -> "th" // 11th, 12th, 13th are special
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }

    /**
     * Pre-split date parts for ordinal rendering.
     */
    data class OrdinalDate(
        val day: Int,
        val suffix: String,
        val month: String,
        val year: Int,
    )

    /**
     * Extracts day of month from epoch seconds.
     */
    fun formatDay(epochSeconds: Long): Int {
        return epochSeconds.toLocalDate().dayOfMonth
    }

    /**
     * Extracts month display name from epoch seconds: "January"
     */
    fun formatMonth(epochSeconds: Long): String {
        return epochSeconds.toLocalDate().month.getDisplayName(
            TextStyle.FULL,
            Locale.getDefault(),
        )
    }

    /**
     * Extracts month value from epoch seconds: 1-12
     */
    fun formatMonthValue(epochSeconds: Long): Int {
        return epochSeconds.toLocalDate().monthValue
    }

    /**
     * Extracts year from epoch seconds.
     */
    fun formatYear(epochSeconds: Long): Int {
        return epochSeconds.toLocalDate().year
    }
}
