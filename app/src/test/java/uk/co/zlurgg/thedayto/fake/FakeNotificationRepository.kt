package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository

/**
 * Fake implementation of NotificationRepository for testing.
 * Simulates notification operations in memory.
 */
open class FakeNotificationRepository : NotificationRepository {

    // In-memory storage for testing
    private var notificationScheduled: Boolean = false
    private var scheduledHour: Int = 9
    private var scheduledMinute: Int = 0
    var hasPermission: Boolean = true
    var shouldSendNotification: Boolean = true
    var setupDailyNotificationCalled: Boolean = false
    var cancelNotificationsCalled: Boolean = false
    var updateNotificationTimeCalled: Boolean = false
    var updateNotificationTimeThrows: Boolean = false

    override fun setupDailyNotification() {
        setupDailyNotificationCalled = true
        notificationScheduled = true
    }

    override fun cancelNotifications() {
        cancelNotificationsCalled = true
        notificationScheduled = false
    }

    override fun updateNotificationTime(hour: Int, minute: Int) {
        if (updateNotificationTimeThrows) {
            throw RuntimeException("Simulated failure")
        }
        updateNotificationTimeCalled = true
        scheduledHour = hour
        scheduledMinute = minute
        notificationScheduled = true
    }

    override fun hasNotificationPermission(): Boolean {
        return hasPermission
    }

    override suspend fun shouldSendNotification(): Boolean {
        return shouldSendNotification
    }

    override fun areSystemNotificationsEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun shouldShowPermissionRationale(): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Helper method to reset all values to defaults.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        notificationScheduled = false
        scheduledHour = 9
        scheduledMinute = 0
        hasPermission = true
        setupDailyNotificationCalled = false
        cancelNotificationsCalled = false
        updateNotificationTimeCalled = false
        updateNotificationTimeThrows = false
    }

    /**
     * Helper method to verify notification was scheduled with correct time.
     */
    fun isScheduledAt(hour: Int, minute: Int): Boolean {
        return notificationScheduled && scheduledHour == hour && scheduledMinute == minute
    }
}
