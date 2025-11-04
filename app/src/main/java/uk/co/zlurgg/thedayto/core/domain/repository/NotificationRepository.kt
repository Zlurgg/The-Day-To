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

    /**
     * Determines if a notification should be sent based on current state.
     *
     * Checks if an entry already exists for today. If user has already
     * logged their mood, no reminder notification should be sent.
     *
     * This encapsulates the business logic decision of when to send notifications,
     * keeping the Worker purely focused on infrastructure concerns.
     *
     * @return true if notification should be sent, false otherwise
     */
    suspend fun shouldSendNotification(): Boolean

    /**
     * Checks if system-level notifications are enabled for this app.
     *
     * This checks the device's notification settings, not just permission.
     * Even with permission granted, user can disable notifications in system settings.
     *
     * @return true if notifications are enabled at system level, false otherwise
     */
    fun areSystemNotificationsEnabled(): Boolean

    /**
     * Checks if we should show a rationale for notification permission.
     *
     * Returns false if:
     * - Permission is permanently denied ("Don't ask again" selected)
     * - Permission has never been requested
     * - Running on API < 33 (no runtime permission needed)
     *
     * Returns true if:
     * - Permission was denied but can be requested again
     *
     * Note: This requires Activity context and should be called from UI layer.
     *
     * @return true if rationale should be shown, false otherwise
     */
    fun shouldShowPermissionRationale(): Boolean
}
