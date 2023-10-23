package com.jbrightman.thedayto.domain.repository

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

class TheDayToPrefRepository(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    private val editor = pref.edit()

    private fun String.put(boolean: Boolean) {
        editor.putBoolean(this, boolean)
        editor.commit()
    }

    private fun String.getBoolean() = pref.getBoolean(this, false)

    fun setDailyEntryCreated(dailyEntryCreated: Boolean) {
        DAILY_ENTRY_CREATED.put(dailyEntryCreated)
    }

    fun getDailyEntryCreated() = DAILY_ENTRY_CREATED.getBoolean()

    private fun String.put(long: Long) {
        editor.putLong(this, long)
        editor.commit()
    }

    private fun String.getDatestamp() = pref.getLong(this, 0L)

    fun setDailyEntryDate(datestamp: Long) {
        DAILY_ENTRY_DATE.put(datestamp)
    }

    fun getDailyEntryDate() = DAILY_ENTRY_DATE.getDatestamp()

}