package uk.co.zlurgg.thedayto.notification.domain.repository

import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
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
     * when (val result = repository.getSettingsState(userId)) {
     *     is Result.Success -> when (result.data) {
     *         is NotConfigured -> showOnboarding()
     *         is Configured -> if (result.data.settings.enabled) showEnabled() else showDisabled()
     *     }
     *     is Result.Error -> showError()
     * }
     * ```
     */
    suspend fun getSettingsState(userId: String): Result<NotificationSettingsState, DataError.Local>

    /**
     * Get settings for internal logic where null indicates no settings.
     *
     * Use this for simpler null checks in background logic:
     * ```
     * when (val result = repository.getSettings(userId)) {
     *     is Result.Success -> result.data?.let { if (it.enabled) { ... } }
     *     is Result.Error -> log error
     * }
     * ```
     */
    suspend fun getSettings(userId: String): Result<NotificationSettings?, DataError.Local>

    /**
     * Save or update settings for a user.
     *
     * Creates a new record if none exists, updates existing record otherwise.
     * Sets sync status to PENDING_SYNC for later cloud synchronization.
     */
    suspend fun saveSettings(userId: String, settings: NotificationSettings): EmptyResult<DataError.Local>

    /**
     * Delete settings for a user.
     *
     * Called on sign-out to clean up user data.
     */
    suspend fun deleteSettings(userId: String): EmptyResult<DataError.Local>
}
