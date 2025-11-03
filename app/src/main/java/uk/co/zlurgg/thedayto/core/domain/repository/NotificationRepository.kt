package uk.co.zlurgg.thedayto.core.domain.repository

/**
 * Repository interface for managing notification operations.
 *
 * Follows Clean Architecture principles by defining the contract in the domain layer,
 * while the implementation resides in the data layer.
 */
interface NotificationRepository {

    /**
     * Sets up daily notification based on user preferences.
     *
     * Should be called after notification permission is granted.
     * Uses WorkManager to schedule the notification at the user-configured time.
     * If notifications are disabled in preferences, this does nothing.
     */
    fun setupDailyNotification()

    /**
     * Cancels all scheduled notifications.
     * Useful when user signs out or disables notifications.
     */
    fun cancelNotifications()

    /**
     * Updates the notification schedule with a new time.
     * Cancels existing notification and schedules a new one.
     *
     * @param hour hour in 24-hour format (0-23)
     * @param minute minute (0-59)
     */
    fun updateNotificationTime(hour: Int, minute: Int)

    /**
     * Checks if the app has notification permission.
     *
     * @return true if permission is granted, false otherwise.
     * On API < 33, always returns true (permission not required).
     */
    fun hasNotificationPermission(): Boolean
}
