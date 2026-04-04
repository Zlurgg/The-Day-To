package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings
import uk.co.zlurgg.thedayto.notification.domain.repository.NotificationSettingsRepository

/**
 * Gets notification settings from Room storage.
 *
 * Returns settings for the current user (signed-in or anonymous).
 * If no settings exist, returns default settings (disabled, 9:00 AM).
 */
class GetNotificationSettingsUseCase(
    private val settingsRepository: NotificationSettingsRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): NotificationSettings {
        val userId = authRepository.getSignedInUser()?.userId ?: ANONYMOUS_USER_ID
        return settingsRepository.getSettings(userId) ?: NotificationSettings()
    }
}
