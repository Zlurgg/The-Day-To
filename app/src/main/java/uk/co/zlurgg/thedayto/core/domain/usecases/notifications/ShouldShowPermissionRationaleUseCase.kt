package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository

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
 * - Delegates to repository for platform-specific implementation
 *
 * @param notificationRepository Repository for notification operations
 */
class ShouldShowPermissionRationaleUseCase(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(): Boolean {
        return notificationRepository.shouldShowPermissionRationale()
    }
}