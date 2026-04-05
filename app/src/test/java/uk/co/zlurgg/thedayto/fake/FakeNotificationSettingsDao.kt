package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.notification.data.local.NotificationSettingsDao
import uk.co.zlurgg.thedayto.notification.data.local.NotificationSettingsEntity

/**
 * Fake implementation of NotificationSettingsDao for testing.
 * Stores entities in memory.
 */
class FakeNotificationSettingsDao : NotificationSettingsDao {

    private val settingsMap = mutableMapOf<String, NotificationSettingsEntity>()

    override suspend fun getByUserId(userId: String): NotificationSettingsEntity? {
        return settingsMap[userId]
    }

    override suspend fun upsert(settings: NotificationSettingsEntity) {
        settingsMap[settings.userId] = settings
    }

    override suspend fun deleteByUserId(userId: String) {
        settingsMap.remove(userId)
    }

    override suspend fun updateSyncStatus(userId: String, status: String) {
        settingsMap[userId]?.let {
            settingsMap[userId] = it.copy(syncStatus = status)
        }
    }

    override suspend fun getPendingSync(userId: String): NotificationSettingsEntity? {
        return settingsMap[userId]?.takeIf { it.syncStatus == "PENDING_SYNC" }
    }

    override suspend fun updateLastNotifiedDate(userId: String, dateEpoch: Long) {
        settingsMap[userId]?.let {
            settingsMap[userId] = it.copy(
                lastNotifiedDateEpoch = dateEpoch,
                syncStatus = "PENDING_SYNC"
            )
        }
    }

    override suspend fun deleteAll() {
        settingsMap.clear()
    }

    /**
     * Helper for verification in tests.
     */
    fun getAllSettings(): Map<String, NotificationSettingsEntity> {
        return settingsMap.toMap()
    }

    /**
     * Helper to reset state between tests.
     */
    fun reset() {
        settingsMap.clear()
    }
}
