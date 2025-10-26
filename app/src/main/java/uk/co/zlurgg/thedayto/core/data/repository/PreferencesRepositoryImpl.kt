package uk.co.zlurgg.thedayto.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import uk.co.zlurgg.thedayto.core.data.util.ENTRY_CREATED
import uk.co.zlurgg.thedayto.core.data.util.ENTRY_DATE
import uk.co.zlurgg.thedayto.core.data.util.PREFERENCE_NAME
import uk.co.zlurgg.thedayto.core.data.util.SIGNED_IN_STATE
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

class PreferencesRepositoryImpl(context: Context) : PreferencesRepository {
    private val pref: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    private val editor = pref.edit()

    private fun String.put(boolean: Boolean) {
        editor.putBoolean(this, boolean)
        editor.commit()
    }

    private fun String.getBoolean() = pref.getBoolean(this, false)

    override fun entryCreated(entryCreated: Boolean) {
        ENTRY_CREATED.put(entryCreated)
    }

    override fun getEntryCreated() = ENTRY_CREATED.getBoolean()

    private fun String.put(long: Long) {
        editor.putLong(this, long)
        editor.commit()
    }

    private fun String.getDatestamp() = pref.getLong(this, 0L)

    override fun setEntryDate(datestamp: Long) {
        ENTRY_DATE.put(datestamp)
    }

    override fun getEntryDate() = ENTRY_DATE.getDatestamp()

    override fun setSignedInState(isSignedIn: Boolean) {
        SIGNED_IN_STATE.put(isSignedIn)
    }

    override fun getSignedInState() = SIGNED_IN_STATE.getBoolean()

}