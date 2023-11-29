package uk.co.zlurgg.thedayto.presentation.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


fun datestampToFormattedDate(date: Long): String {
    val dt = Instant.ofEpochSecond(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")
    return formatter.format(dt)
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