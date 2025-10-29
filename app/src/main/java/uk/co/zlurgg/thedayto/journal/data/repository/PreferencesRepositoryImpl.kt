package uk.co.zlurgg.thedayto.journal.data.repository

import android.content.Context
import androidx.core.content.edit
import uk.co.zlurgg.thedayto.journal.domain.repository.PreferencesRepository
import java.time.LocalDate

/**
 * Implementation of PreferencesRepository using Android SharedPreferences
 *
 * Manages journal-specific preferences and settings persistence.
 * Uses SharedPreferences for simple key-value storage.
 *
 * @param context Application context for accessing SharedPreferences
 */
class PreferencesRepositoryImpl(
    context: Context
) : PreferencesRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if the entry reminder dialog has already been shown today
     *
     * Compares the stored date string with today's date to determine
     * if the reminder has already been displayed.
     *
     * @return true if reminder was shown today, false otherwise
     */
    override suspend fun hasShownEntryReminderToday(): Boolean {
        val lastShownDate = prefs.getString(KEY_LAST_REMINDER_DATE, null)
        val today = LocalDate.now().toString()
        return lastShownDate == today
    }

    /**
     * Mark that the entry reminder dialog has been shown today
     *
     * Stores the current date as an ISO-8601 date string (YYYY-MM-DD).
     * This allows checking on future app launches whether the reminder
     * was already shown on the current day.
     */
    override suspend fun markEntryReminderShownToday() {
        val today = LocalDate.now().toString()
        prefs.edit { putString(KEY_LAST_REMINDER_DATE, today) }
    }

    /**
     * Check if this is the user's first launch of the app
     *
     * @return true if first launch flag is not set (first time), false otherwise
     */
    override suspend fun isFirstLaunch(): Boolean {
        return !prefs.getBoolean(KEY_FIRST_LAUNCH_COMPLETE, false)
    }

    /**
     * Mark that the first launch setup has been completed
     *
     * Sets a persistent flag indicating that default mood colors have been
     * seeded and initial setup is complete.
     */
    override suspend fun markFirstLaunchComplete() {
        prefs.edit { putBoolean(KEY_FIRST_LAUNCH_COMPLETE, true) }
    }

    companion object {
        private const val PREFS_NAME = "journal_prefs"
        private const val KEY_LAST_REMINDER_DATE = "last_entry_reminder_date"
        private const val KEY_FIRST_LAUNCH_COMPLETE = "first_launch_complete"
    }
}
