package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
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

    // Control error simulation for testing
    var shouldReturnError: Boolean = false
    var errorToReturn: DataError.Local = DataError.Local.DATABASE_ERROR

    override suspend fun getSettingsState(userId: String): Result<NotificationSettingsState, DataError.Local> {
        if (shouldReturnError) return Result.Error(errorToReturn)

        val settings = settingsMap[userId]
        return if (settings != null) {
            Result.Success(NotificationSettingsState.Configured(settings))
        } else {
            Result.Success(NotificationSettingsState.NotConfigured)
        }
    }

    override suspend fun getSettings(userId: String): Result<NotificationSettings?, DataError.Local> {
        if (shouldReturnError) return Result.Error(errorToReturn)
        return Result.Success(settingsMap[userId])
    }

    override suspend fun saveSettings(userId: String, settings: NotificationSettings): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(errorToReturn)
        settingsMap[userId] = settings
        return Result.Success(Unit)
    }

    override suspend fun deleteSettings(userId: String): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(errorToReturn)
        settingsMap.remove(userId)
        return Result.Success(Unit)
    }

    /**
     * Helper method to set settings directly for testing.
     */
    fun setSettings(userId: String, settings: NotificationSettings) {
        settingsMap[userId] = settings
    }

    /**
     * Helper method to get settings directly for test assertions (bypasses Result wrapper).
     */
    fun getSettingsDirectly(userId: String): NotificationSettings? {
        return settingsMap[userId]
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
        shouldReturnError = false
        errorToReturn = DataError.Local.DATABASE_ERROR
    }
}
