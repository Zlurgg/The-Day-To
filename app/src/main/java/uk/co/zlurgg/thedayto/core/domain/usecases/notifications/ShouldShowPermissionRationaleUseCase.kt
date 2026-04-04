package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import uk.co.zlurgg.thedayto.notification.domain.scheduler.NotificationScheduler

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
 * Following Clean Architecture:
 * - Single responsibility: Check permission rationale state
 * - Pure domain layer - no Android framework dependencies
 * - Delegates to scheduler for platform-specific implementation
 *
 * @param notificationScheduler Scheduler for notification operations
 */
class ShouldShowPermissionRationaleUseCase(
    private val notificationScheduler: NotificationScheduler
) {
    operator fun invoke(): Boolean {
        return notificationScheduler.shouldShowPermissionRationale()
    }
}