package uk.co.zlurgg.thedayto.journal.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale


fun datestampToFormattedDate(date: Long): String {
    val dt = Instant.ofEpochSecond(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    // Use explicit locale formatting for consistency
    val day = dt.dayOfMonth.toString().padStart(2, '0')
    val month = dt.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val year = dt.year

    return "$day $month, $year"
}

fun datestampToDay(date: Long): Int {
    val dt = Instant.ofEpochSecond(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    return dt.dayOfMonth
}

fun dayToDatestampForCurrentMonthAndYear(day: Int, month: Int, year: Int): Long {
    val localDate = LocalDate.of(year, month, day)
    return localDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
}

fun datestampToMonthValue(date: Long): String {
    return Instant.ofEpochSecond(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().monthValue.toString()
}

fun datestampToYearValue(date: Long): String {
    return Instant.ofEpochSecond(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().year.toString()
}