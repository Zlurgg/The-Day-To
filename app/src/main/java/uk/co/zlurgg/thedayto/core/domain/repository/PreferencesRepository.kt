package uk.co.zlurgg.thedayto.core.domain.repository

/**
 * Repository interface for managing app-wide preferences.
 *
 * Provides access to user preferences stored in SharedPreferences.
 * This is a core infrastructure interface used across multiple features
 * (journal, notifications, auth).
 *
 * Following Clean Architecture:
 * - Domain layer interface (abstraction)
 * - Implemented in data layer (concrete implementation)
 * - Used by use cases and features
 */
interface PreferencesRepository {
    /**
     * Check if the entry reminder dialog has already been shown today.
     *
     * Used to ensure the reminder dialog only appears once per day,
     * providing a gentle nudge without being intrusive.
     *
     * @return true if reminder was already shown today, false otherwise
     */
    suspend fun hasShownEntryReminderToday(): Boolean

    /**
     * Mark that the entry reminder dialog has been shown today.
     *
     * Stores the current date to track when the reminder was last displayed.
     * This prevents showing the dialog multiple times on the same day.
     */
    suspend fun markEntryReminderShownToday()

    /**
     * Check if this is the user's first launch of the app.
     *
     * Used to determine if default mood colors should be seeded
     * and first-time setup should be performed.
     *
     * @return true if this is first launch, false otherwise
     */
    suspend fun isFirstLaunch(): Boolean

    /**
     * Mark that the first launch setup has been completed.
     *
     * Called after seeding default mood colors and completing
     * initial app setup.
     */
    suspend fun markFirstLaunchComplete()

    /**
     * Check if the user has seen the welcome dialog.
     *
     * Used to determine if the welcome dialog should be shown
     * on the sign-in screen for first-time users.
     *
     * @return true if user has seen the welcome dialog, false otherwise
     */
    suspend fun hasSeenWelcomeDialog(): Boolean

    /**
     * Mark that the user has seen the welcome dialog.
     *
     * Called after the welcome dialog is dismissed on first launch.
     */
    suspend fun markWelcomeDialogSeen()

    /**
     * Check if the user has seen the editor tutorial.
     *
     * Used to determine if the editor tutorial should be shown
     * on the first entry creation.
     *
     * @return true if user has seen the editor tutorial, false otherwise
     */
    suspend fun hasSeenEditorTutorial(): Boolean

    /**
     * Mark that the user has seen the editor tutorial.
     *
     * Called after the editor tutorial is dismissed on first entry creation.
     */
    suspend fun markEditorTutorialSeen()

    /**
     * Check if daily notifications are enabled.
     *
     * @return true if notifications are enabled, false otherwise
     */
    suspend fun isNotificationEnabled(): Boolean

    /**
     * Enable or disable daily notifications.
     *
     * @param enabled true to enable notifications, false to disable
     */
    suspend fun setNotificationEnabled(enabled: Boolean)

    /**
     * Get the hour for daily notifications (0-23).
     *
     * @return hour in 24-hour format, defaults to 9 (9 AM)
     */
    suspend fun getNotificationHour(): Int

    /**
     * Get the minute for daily notifications (0-59).
     *
     * @return minute, defaults to 0
     */
    suspend fun getNotificationMinute(): Int

    /**
     * Set the time for daily notifications.
     *
     * @param hour hour in 24-hour format (0-23)
     * @param minute minute (0-59)
     */
    suspend fun setNotificationTime(hour: Int, minute: Int)
}
