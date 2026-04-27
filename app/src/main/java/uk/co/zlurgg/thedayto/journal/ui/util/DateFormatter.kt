package uk.co.zlurgg.thedayto.journal.ui.util

import uk.co.zlurgg.thedayto.core.data.util.toLocalDate
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Single source of truth for date formatting across the app.
 *
 * All UI date rendering should go through this object so the app
 * presents a consistent style. Uses pure extension functions for
 * epoch conversion — no injected dependencies needed.
 *
 * **Formats:**
 * - [formatDateCompact]: "15th Jan 2024" — entry cards, dialogs, secondary labels
 * - [formatDateOrdinal]: [OrdinalDate] parts for superscript rendering — Editor heading
 * - [formatMonthYear]: "January 2024" — calendar header
 * - [formatMonthShort]: "Jan" — month picker chips
 */
object DateFormatter {

    // ==================== Full-date formats ====================

    /**
     * Compact date with ordinal: "15th Jan 2024".
     * Used in entry cards, delete dialogs, and other space-constrained contexts.
     */
    fun formatDateCompact(epochSeconds: Long): String {
        val date = epochSeconds.toLocalDate()
        val day = date.dayOfMonth
        val suffix = ordinalSuffix(day)
        val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        return "$day$suffix $month ${date.year}"
    }

    /**
     * Returns pre-split parts for rendering a date with superscript ordinal
     * and full month name, e.g. "15ᵗʰ January 2024".
     *
     * Used in the Editor heading where the date is the primary element.
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
     * Returns pre-split parts for rendering a date with superscript ordinal
     * and short month name, e.g. "15ᵗʰ Jan 2024".
     *
     * Used in Overview entry cards where horizontal space is constrained.
     */
    fun formatDateOrdinalCompact(epochSeconds: Long): OrdinalDate {
        val date = epochSeconds.toLocalDate()
        return OrdinalDate(
            day = date.dayOfMonth,
            suffix = ordinalSuffix(date.dayOfMonth),
            month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            year = date.year,
        )
    }

    /**
     * Pre-split date parts for ordinal rendering with superscript.
     */
    data class OrdinalDate(
        val day: Int,
        val suffix: String,
        val month: String,
        val year: Int,
    )

    // ==================== Month/year formats ====================

    /**
     * Full month name + year: "January 2024".
     * Used in the calendar header.
     */
    fun formatMonthYear(date: LocalDate): String {
        val month = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        return "$month ${date.year}"
    }

    /**
     * Short month name: "Jan".
     * Used in month picker chips.
     */
    fun formatMonthShort(date: LocalDate): String {
        return date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    /**
     * Short month, day, and year: "Jan 15, 2024".
     * Used in stats for first entry date display.
     */
    fun formatDateShort(date: LocalDate): String {
        val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        return "$month ${date.dayOfMonth}, ${date.year}"
    }

    // ==================== Component helpers ====================

    /**
     * Extracts day of month from epoch seconds.
     */
    fun formatDay(epochSeconds: Long): Int {
        return epochSeconds.toLocalDate().dayOfMonth
    }

    /**
     * Extracts month value from epoch seconds: 1-12.
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

    // ==================== Internal ====================

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
}
