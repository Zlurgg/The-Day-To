package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import uk.co.zlurgg.thedayto.notification.domain.scheduler.NotificationScheduler

/**
 * Use case for checking if notification permission is granted.
 *
 * Returns true if permission is granted, false otherwise.
 * On API < 33, always returns true (permission not required).
 */
class CheckNotificationPermissionUseCase(
    private val notificationScheduler: NotificationScheduler
) {
    operator fun invoke(): Boolean {
        return notificationScheduler.hasNotificationPermission()
    }
}
