package uk.co.zlurgg.thedayto.notification.domain.repository

import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettingsState

/**
 * Repository for notification settings persistence.
 *
 * Provides both sealed state access ([getSettingsState]) for UI and
 * nullable access ([getSettings]) for internal logic.
 *
 * Settings are stored per-user:
 * - Anonymous users use "anonymous" as userId
 * - Signed-in users use their Firebase UID
 */
interface NotificationSettingsRepository {

    /**
     * Get settings as a sealed state for UI display.
     *
     * Use this when you need to distinguish between "not configured" and "disabled":
     * ```
     * when (val state = repository.getSettingsState(userId)) {
     *     is NotConfigured -> showOnboarding()
     *     is Configured -> if (state.settings.enabled) showEnabled() else showDisabled()
     * }
     * ```
     */
    suspend fun getSettingsState(userId: String): NotificationSettingsState

    /**
     * Get settings for internal logic where null indicates no settings.
     *
     * Use this for simpler null checks in background logic:
     * ```
     * val settings = repository.getSettings(userId) ?: return
     * if (settings.enabled) { ... }
     * ```
     */
    suspend fun getSettings(userId: String): NotificationSettings?

    /**
     * Save or update settings for a user.
     *
     * Creates a new record if none exists, updates existing record otherwise.
     * Sets sync status to PENDING_SYNC for later cloud synchronization.
     */
    suspend fun saveSettings(userId: String, settings: NotificationSettings)

    /**
     * Delete settings for a user.
     *
     * Called on sign-out to clean up user data.
     */
    suspend fun deleteSettings(userId: String)
}
