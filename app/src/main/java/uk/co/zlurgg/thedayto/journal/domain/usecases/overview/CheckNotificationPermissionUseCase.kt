package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository

/**
 * Checks if notification permission is granted.
 *
 * @return true if permission is granted, false otherwise
 */
class CheckNotificationPermissionUseCase(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(): Boolean {
        return notificationRepository.hasNotificationPermission()
    }
}
