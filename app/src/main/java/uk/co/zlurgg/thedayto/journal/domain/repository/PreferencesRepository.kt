package uk.co.zlurgg.thedayto.journal.domain.repository

/**
 * Repository interface for managing journal-related preferences
 *
 * Provides access to journal-specific user preferences stored
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

    /**
     * Check if this is the user's first launch of the app
     *
     * Used to determine if default mood colors should be seeded
     * and first-time setup should be performed.
     *
     * @return true if this is first launch, false otherwise
     */
    suspend fun isFirstLaunch(): Boolean

    /**
     * Mark that the first launch setup has been completed
     *
     * Called after seeding default mood colors and completing
     * initial app setup.
     */
    suspend fun markFirstLaunchComplete()
}
