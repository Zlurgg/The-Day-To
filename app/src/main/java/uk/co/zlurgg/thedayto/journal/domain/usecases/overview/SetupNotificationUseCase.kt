package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository

/**
 * Sets up daily notification scheduling.
 * Called after notification permission is granted.
 */
class SetupNotificationUseCase(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke() {
        notificationRepository.setupDailyNotificationIfNeeded()
    }
}
