package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository

/**
 * Use case for checking if notification permission is granted.
 *
 * Returns true if permission is granted, false otherwise.
 * On API < 33, always returns true (permission not required).
 */
class CheckNotificationPermissionUseCase(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(): Boolean {
        return notificationRepository.hasNotificationPermission()
    }
}
