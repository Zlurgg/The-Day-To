package uk.co.zlurgg.thedayto.journal.ui.overview.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Constants for calendar layout and styling.
 */
object CalendarConstants {
    // Layout
    const val DAYS_IN_WEEK = 7
    const val INITIAL_PAGER_PAGE = 1_000_000_000

    // Sizing
    val DEFAULT_DAY_SIZE_MAX: Dp = 48.dp
    val CALENDAR_HORIZONTAL_PADDING: Dp = 16.dp
    val CALENDAR_DAY_SPACING: Dp = 8.dp
    val CALENDAR_ROW_SPACING: Dp = 4.dp
    val CALENDAR_BOTTOM_PADDING: Dp = 16.dp
    val BUFFER_SIZE: Dp = 1.dp

    // Day of week header
    val DAY_HEADER_BOTTOM_PADDING: Dp = 4.dp

    // Borders
    val TODAY_BORDER_WIDTH: Dp = 2.dp

    // Alpha values for day states
    object DayAlpha {
        const val TODAY = 1f
        const val PAST = 0.7f
        const val FUTURE = 0.3f
    }
}
