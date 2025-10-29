package uk.co.zlurgg.thedayto.core.domain.repository

/**
 * Repository interface for managing application preferences
 *
 * Provides access to user preferences and app-level settings stored
 * in SharedPreferences. Domain layer interface - implemented in data layer.
 */
interface PreferencesRepository {
    /**
     * Check if the entry reminder dialog has already been shown today
     *
     * Used to ensure the reminder dialog only appears once per day,
     * providing a gentle nudge without being intrusive.
     *
     * @return true if reminder was already shown today, false otherwise
     */
    suspend fun hasShownEntryReminderToday(): Boolean

    /**
     * Mark that the entry reminder dialog has been shown today
     *
     * Stores the current date to track when the reminder was last displayed.
     * This prevents showing the dialog multiple times on the same day.
     */
    suspend fun markEntryReminderShownToday()
}
