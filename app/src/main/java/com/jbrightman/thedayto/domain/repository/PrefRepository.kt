package com.jbrightman.thedayto.domain.repository

import android.content.Context
import android.content.SharedPreferences
class PrefRepository(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    private val editor = pref.edit()

    private fun String.put(boolean: Boolean) {
        editor.putBoolean(this, boolean)
        editor.commit()
    }

    private fun String.getBoolean() = pref.getBoolean(this, false)

    fun setDailyEntryCreated(dailyEntryCreated: Boolean) {
        PREF_DAILY_ENTRY_CREATED.put(dailyEntryCreated)
    }

    fun getDailyEntryCreated() = PREF_DAILY_ENTRY_CREATED.getBoolean()
}