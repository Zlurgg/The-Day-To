package uk.co.zlurgg.thedayto.journal.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale

/**
 * @deprecated Use [DateFormatter] instead. These functions will be removed in a future release.
 */

@Deprecated(
    message = "Use DateFormatter.formatDate()",
    replaceWith = ReplaceWith(
        expression = "DateFormatter.formatDate(date)",
        imports = ["uk.co.zlurgg.thedayto.journal.ui.util.DateFormatter"]
    )
)
fun datestampToFormattedDate(date: Long): String {
    val dt = Instant.ofEpochSecond(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    // Use explicit locale formatting for consistency
    val day = dt.dayOfMonth.toString().padStart(2, '0')
    val month = dt.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val year = dt.year

    return "$day $month $year"
}

@Deprecated(
    message = "Use DateFormatter.formatDay()",
    replaceWith = ReplaceWith(
        expression = "DateFormatter.formatDay(date)",
        imports = ["uk.co.zlurgg.thedayto.journal.ui.util.DateFormatter"]
    )
)
fun datestampToDay(date: Long): Int {
    val dt = Instant.ofEpochSecond(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    return dt.dayOfMonth
}

@Deprecated(
    message = "Use CalendarHelper.dayToStorageEpoch()",
    replaceWith = ReplaceWith(
        expression = "calendarHelper.dayToStorageEpoch(day, month, year)",
        imports = ["uk.co.zlurgg.thedayto.journal.ui.util.CalendarHelper"]
    )
)
fun dayToDatestampForCurrentMonthAndYear(day: Int, month: Int, year: Int): Long {
    val localDate = LocalDate.of(year, month, day)
    return localDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
}

@Deprecated(
    message = "Use DateFormatter.formatMonthValue()",
    replaceWith = ReplaceWith(
        expression = "DateFormatter.formatMonthValue(date).toString()",
        imports = ["uk.co.zlurgg.thedayto.journal.ui.util.DateFormatter"]
    )
)
fun datestampToMonthValue(date: Long): String {
    return Instant.ofEpochSecond(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().monthValue.toString()
}

@Deprecated(
    message = "Use DateFormatter.formatYear()",
    replaceWith = ReplaceWith(
        expression = "DateFormatter.formatYear(date).toString()",
        imports = ["uk.co.zlurgg.thedayto.journal.ui.util.DateFormatter"]
    )
)
fun datestampToYearValue(date: Long): String {
    return Instant.ofEpochSecond(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().year.toString()
}
