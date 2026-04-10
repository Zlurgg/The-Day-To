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
