package com.example.thedayto

import java.text.SimpleDateFormat
import java.time.Month
import java.util.Calendar
import java.util.Locale

class DateUtil() {

    fun getCurrentDate(): String {
        val c: Calendar = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1
        val day = c.get(Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }

    fun getCurrentMonthInMMMMFormat(): String {
        val c: Calendar = Calendar.getInstance()
        val date = SimpleDateFormat("MMMM", Locale.ENGLISH)
        return date.format(c.time)
    }

    fun changeMonthFromMMMMToMMFormat(input: String): Int {
        return when (input.lowercase()) {
            "january" -> { 1 }
            "february" -> { 2 }
            "march" -> { 3 }
            "april" -> { 4 }
            "may" -> { 5 }
            "june" -> { 6 }
            "july" -> { 7 }
            "august" -> { 8 }
            "september" -> { 9 }
            "october" -> { 10 }
            "november" -> { 11 }
            "december" -> { 12 }
            else -> { 0 }
        }
    }

    fun getNumberOfDaysInCurrentMonth(): Int {
        val c: Calendar = Calendar.getInstance()
        return c.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}