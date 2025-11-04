package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository

/**
 * Use case to check if notifications are enabled at the system level.
 *
 * This is separate from Android 13+ runtime permission (POST_NOTIFICATIONS).
 * Even with permission granted, users can disable notifications in Android Settings.
 *
 * Use Cases:
 * - Before enabling notifications: Warn if system notifications are disabled
 * - Troubleshooting: Help users understand why notifications aren't appearing
 *
 * Following Clean Architecture:
 * - Single responsibility: Check system notification state
 * - Pure domain layer - no Android framework dependencies
 * - Delegates to repository for platform-specific implementation
 * - Used by ViewModels to make decisions about notification settings
 *
 * @param notificationRepository Repository for notification operations
 */
class CheckSystemNotificationsEnabledUseCase(
    private val notificationRepository: NotificationRepository
) {
    /**
     * Check if notifications are enabled for this app in system settings.
     *
     * Checks the master notification toggle in Android Settings > Apps > App > Notifications.
     * This is independent of:
     * - Runtime permission (POST_NOTIFICATIONS for Android 13+)
     * - Notification channels (can be enabled/disabled individually)
     * - Do Not Disturb mode
     *
     * @return true if system notifications are enabled, false otherwise
     */
    operator fun invoke(): Boolean {
        return notificationRepository.areSystemNotificationsEnabled()
    }
}