package com.jbrightman.thedayto.feature_thedayto.presentation.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun longToFormattedDateText(date: Long): String {
    val dt = Instant.ofEpochSecond(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")
    return formatter.format(dt)
}
