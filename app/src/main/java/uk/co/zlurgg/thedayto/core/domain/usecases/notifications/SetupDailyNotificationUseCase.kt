package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository

/**
 * Use case for setting up the daily notification schedule.
 *
 * Acts as a fail-safe to ensure periodic notifications are scheduled on app startup.
 * Safe to call multiple times - will only schedule if notifications are enabled
 * and will use ExistingPeriodicWorkPolicy.KEEP to avoid resetting existing work.
 *
 * Handles:
 * - First-time users: Does nothing (notifications disabled by default)
 * - Existing users with notifications OFF: Does nothing
 * - Existing users with notifications ON: Ensures periodic work is scheduled
 */
class SetupDailyNotificationUseCase(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke() {
        notificationRepository.setupDailyNotification()
    }
}
