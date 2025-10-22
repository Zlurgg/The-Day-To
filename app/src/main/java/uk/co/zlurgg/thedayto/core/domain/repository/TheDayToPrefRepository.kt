package uk.co.zlurgg.thedayto.core.domain.repository

import android.content.Context
import android.content.SharedPreferences

class PreferencesRepositoryImpl(context: Context) : PreferencesRepository {
    private val pref: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    private val editor = pref.edit()

    private fun String.put(boolean: Boolean) {
        editor.putBoolean(this, boolean)
        editor.commit()
    }

    private fun String.getBoolean() = pref.getBoolean(this, false)

    override fun setDailyEntryCreated(dailyEntryCreated: Boolean) {
        DAILY_ENTRY_CREATED.put(dailyEntryCreated)
    }

    override fun getDailyEntryCreated() = DAILY_ENTRY_CREATED.getBoolean()

    private fun String.put(long: Long) {
        editor.putLong(this, long)
        editor.commit()
    }

    private fun String.getDatestamp() = pref.getLong(this, 0L)

    override fun setDailyEntryDate(datestamp: Long) {
        DAILY_ENTRY_DATE.put(datestamp)
    }

    override fun getDailyEntryDate() = DAILY_ENTRY_DATE.getDatestamp()

}

// Keeping old class name for backward compatibility - can be removed after all usages are updated
@Deprecated("Use PreferencesRepository interface and PreferencesRepositoryImpl instead", ReplaceWith("PreferencesRepositoryImpl"))
typealias TheDayToPrefRepository = PreferencesRepositoryImpl