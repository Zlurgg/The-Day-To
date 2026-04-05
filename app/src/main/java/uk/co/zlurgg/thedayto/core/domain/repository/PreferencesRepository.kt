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

    // ==================== Cloud Sync ====================

    /**
     * Check if cloud sync is enabled.
     *
     * When enabled, local data is synced to Firestore for cross-device access.
     *
     * @return true if sync is enabled, false otherwise (default: false)
     */
    suspend fun isSyncEnabled(): Boolean

    /**
     * Enable or disable cloud sync.
     *
     * When disabled, data remains local only. When enabled, triggers initial sync.
     *
     * @param enabled true to enable sync, false to disable
     */
    suspend fun setSyncEnabled(enabled: Boolean)

    /**
     * Get the timestamp of the last successful sync.
     *
     * Used to track sync freshness and detect stale data.
     *
     * @return epoch millis of last sync, or null if never synced
     */
    suspend fun getLastSyncTimestamp(): Long?

    /**
     * Set the timestamp of the last successful sync.
     *
     * Called after a successful sync operation completes.
     *
     * @param timestamp epoch millis of the sync completion
     */
    suspend fun setLastSyncTimestamp(timestamp: Long)

    /**
     * Clear all preferences.
     *
     * Used during account deletion to reset all user preferences.
     */
    suspend fun clear()
}
