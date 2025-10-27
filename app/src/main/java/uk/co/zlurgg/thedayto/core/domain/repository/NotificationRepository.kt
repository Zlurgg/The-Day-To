package uk.co.zlurgg.thedayto.core.domain.repository

/**
 * Repository interface for managing notification operations.
 *
 * Follows Clean Architecture principles by defining the contract in the domain layer,
 * while the implementation resides in the data layer.
 */
interface NotificationRepository {

    /**
     * Sets up daily notification if the user has made an entry yesterday
     * or is a first-time user (entry date = 0).
     *
     * Should be called after notification permission is granted.
     * Uses WorkManager to schedule the notification.
     */
    fun setupDailyNotificationIfNeeded()

    /**
     * Cancels all scheduled notifications.
     * Useful when user signs out or disables notifications.
     */
    fun cancelNotifications()

    /**
     * Checks if the app has notification permission.
     *
     * @return true if permission is granted, false otherwise.
     * On API < 33, always returns true (permission not required).
     */
    fun hasNotificationPermission(): Boolean
}
