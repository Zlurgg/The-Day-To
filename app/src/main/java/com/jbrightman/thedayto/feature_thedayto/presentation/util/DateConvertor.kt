package com.jbrightman.thedayto.feature_thedayto.presentation.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun datestampToFormattedDate(date: Long): String {
    val dt = Instant.ofEpochSecond(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")
    return formatter.format(dt)
}

fun datestampToFormattedDay(date: Long): String {
    val dt = Instant.ofEpochSecond(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("d")
    return formatter.format(dt)
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