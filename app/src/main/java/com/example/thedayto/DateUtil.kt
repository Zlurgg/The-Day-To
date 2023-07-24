package com.example.thedayto

import java.text.SimpleDateFormat
import java.util.Calendar

class DateUtil() {
    private val c: Calendar = Calendar.getInstance()

    fun getCurrentDate(): String {
        val year = c.get(Calendar.YEAR).toString()
        val month = c.get(Calendar.MONTH).toString()
        val day = c.get(Calendar.DAY_OF_MONTH).toString()
        return "$year-$month-$day"
    }

    fun getCurrentMonthName(): String {
        val date = SimpleDateFormat("MMMM")
        return date.format(c.time)
    }

    fun getNumberOfDaysInCurrentMonth(): Int {
        return c.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}