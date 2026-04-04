package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings
import uk.co.zlurgg.thedayto.notification.domain.repository.NotificationSettingsRepository

/**
 * Saves notification settings to Room and updates notification schedule.
 *
 * Saves settings for the current user (signed-in or anonymous).
 * Also schedules/cancels notifications based on the enabled flag.
 */
class SaveNotificationSettingsUseCase(
    private val settingsRepository: NotificationSettingsRepository,
    private val scheduler: NotificationRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(enabled: Boolean, hour: Int, minute: Int) {
        val userId = authRepository.getSignedInUser()?.userId ?: ANONYMOUS_USER_ID
        val settings = NotificationSettings(enabled = enabled, hour = hour, minute = minute)

        // Save settings to Room
        settingsRepository.saveSettings(userId, settings)

        // Update notification schedule
        if (enabled) {
            scheduler.updateNotificationTime(hour, minute)
        } else {
            scheduler.cancelNotifications()
        }
    }
}
