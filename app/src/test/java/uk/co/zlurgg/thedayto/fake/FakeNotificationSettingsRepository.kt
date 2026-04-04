package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettingsState
import uk.co.zlurgg.thedayto.notification.domain.repository.NotificationSettingsRepository

/**
 * Fake implementation of NotificationSettingsRepository for testing.
 * Stores settings in memory keyed by userId.
 */
class FakeNotificationSettingsRepository : NotificationSettingsRepository {

    // In-memory storage for settings per user
    private val settingsMap = mutableMapOf<String, NotificationSettings>()

    override suspend fun getSettingsState(userId: String): NotificationSettingsState {
        val settings = settingsMap[userId]
        return if (settings != null) {
            NotificationSettingsState.Configured(settings)
        } else {
            NotificationSettingsState.NotConfigured
        }
    }

    override suspend fun getSettings(userId: String): NotificationSettings? {
        return settingsMap[userId]
    }

    override suspend fun saveSettings(userId: String, settings: NotificationSettings) {
        settingsMap[userId] = settings
    }

    override suspend fun deleteSettings(userId: String) {
        settingsMap.remove(userId)
    }

    /**
     * Helper method to set settings directly for testing.
     */
    fun setSettings(userId: String, settings: NotificationSettings) {
        settingsMap[userId] = settings
    }

    /**
     * Helper method to get all stored settings for verification.
     */
    fun getAllSettings(): Map<String, NotificationSettings> {
        return settingsMap.toMap()
    }

    /**
     * Helper method to reset all values to defaults.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        settingsMap.clear()
    }
}
