package uk.co.zlurgg.thedayto.core.data.repository

import android.content.Context
import androidx.core.content.edit
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import io.github.zlurgg.update.domain.repository.UpdatePreferencesRepository
import java.time.LocalDate

/**
 * Implementation of PreferencesRepository using Android SharedPreferences.
 *
 * Manages app-wide preferences and settings persistence.
 * Uses SharedPreferences for simple key-value storage.
 *
 * This is core infrastructure used across multiple features:
 * - Journal: entry reminders, first launch setup
 * - Notifications: notification settings (enabled, time)
 * - Auth: welcome dialog tracking
 * - Update: dismissed version tracking
 *
 * Also implements UpdatePreferencesRepository to allow the update package
 * to be used independently without depending on the full PreferencesRepository.
 *
 * @param context Application context for accessing SharedPreferences
 */
class PreferencesRepositoryImpl(
    context: Context
) : PreferencesRepository, UpdatePreferencesRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if the entry reminder dialog has already been shown today.
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
     * Mark that the entry reminder dialog has been shown today.
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
     * Check if this is the user's first launch of the app.
     *
     * @return true if first launch flag is not set (first time), false otherwise
     */
    override suspend fun isFirstLaunch(): Boolean {
        return !prefs.getBoolean(KEY_FIRST_LAUNCH_COMPLETE, false)
    }

    /**
     * Mark that the first launch setup has been completed.
     *
     * Sets a persistent flag indicating that default mood colors have been
     * seeded and initial setup is complete.
     */
    override suspend fun markFirstLaunchComplete() {
        prefs.edit { putBoolean(KEY_FIRST_LAUNCH_COMPLETE, true) }
    }

    /**
     * Check if the user has seen the welcome dialog.
     *
     * @return true if user has seen welcome dialog, false otherwise (first time)
     */
    override suspend fun hasSeenWelcomeDialog(): Boolean {
        return prefs.getBoolean(KEY_WELCOME_DIALOG_SEEN, false)
    }

    /**
     * Mark that the user has seen the welcome dialog.
     *
     * Sets a persistent flag indicating the welcome dialog was displayed
     * and dismissed by the user.
     */
    override suspend fun markWelcomeDialogSeen() {
        prefs.edit { putBoolean(KEY_WELCOME_DIALOG_SEEN, true) }
    }

    /**
     * Check if the user has seen the editor tutorial.
     *
     * @return true if user has seen editor tutorial, false otherwise (first time)
     */
    override suspend fun hasSeenEditorTutorial(): Boolean {
        return prefs.getBoolean(KEY_EDITOR_TUTORIAL_SEEN, false)
    }

    /**
     * Mark that the user has seen the editor tutorial.
     *
     * Sets a persistent flag indicating the editor tutorial was displayed
     * and dismissed by the user on first entry creation.
     */
    override suspend fun markEditorTutorialSeen() {
        prefs.edit { putBoolean(KEY_EDITOR_TUTORIAL_SEEN, true) }
    }

    /**
     * Check if daily notifications are enabled.
     *
     * @return true if notifications are enabled, false otherwise (default: false)
     */
    override suspend fun isNotificationEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_ENABLED, false)
    }

    /**
     * Enable or disable daily notifications.
     *
     * When notifications are disabled, scheduled notifications should be cancelled.
     * When enabled, notifications should be scheduled based on the configured time.
     *
     * @param enabled true to enable notifications, false to disable
     */
    override suspend fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFICATION_ENABLED, enabled) }
    }

    /**
     * Get the hour for daily notifications (0-23).
     *
     * @return hour in 24-hour format, defaults to 9 (9 AM)
     */
    override suspend fun getNotificationHour(): Int {
        return prefs.getInt(KEY_NOTIFICATION_HOUR, DEFAULT_NOTIFICATION_HOUR)
    }

    /**
     * Get the minute for daily notifications (0-59).
     *
     * @return minute, defaults to 0
     */
    override suspend fun getNotificationMinute(): Int {
        return prefs.getInt(KEY_NOTIFICATION_MINUTE, DEFAULT_NOTIFICATION_MINUTE)
    }

    /**
     * Set the time for daily notifications.
     *
     * Stores the notification time in 24-hour format.
     * After updating, notifications should be rescheduled with the new time.
     *
     * @param hour hour in 24-hour format (0-23)
     * @param minute minute (0-59)
     */
    override suspend fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit {
            putInt(KEY_NOTIFICATION_HOUR, hour)
            putInt(KEY_NOTIFICATION_MINUTE, minute)
        }
    }

    /**
     * Get the version the user has dismissed (opted out of updating to).
     *
     * @return version string that was dismissed, or null if none
     */
    override suspend fun getDismissedVersion(): String? {
        return prefs.getString(KEY_DISMISSED_VERSION, null)
    }

    /**
     * Set the version the user has dismissed.
     *
     * Stores the version so the user won't be prompted about this update again.
     *
     * @param version the version string to mark as dismissed
     */
    override suspend fun setDismissedVersion(version: String) {
        prefs.edit { putString(KEY_DISMISSED_VERSION, version) }
    }

    companion object {
        private const val PREFS_NAME = "journal_prefs"
        private const val KEY_LAST_REMINDER_DATE = "last_entry_reminder_date"
        private const val KEY_FIRST_LAUNCH_COMPLETE = "first_launch_complete"
        private const val KEY_WELCOME_DIALOG_SEEN = "welcome_dialog_seen"
        private const val KEY_EDITOR_TUTORIAL_SEEN = "editor_tutorial_seen"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_NOTIFICATION_HOUR = "notification_hour"
        private const val KEY_NOTIFICATION_MINUTE = "notification_minute"
        private const val KEY_DISMISSED_VERSION = "dismissed_update_version"

        private const val DEFAULT_NOTIFICATION_HOUR = 9  // 9 AM
        private const val DEFAULT_NOTIFICATION_MINUTE = 0
    }
}
