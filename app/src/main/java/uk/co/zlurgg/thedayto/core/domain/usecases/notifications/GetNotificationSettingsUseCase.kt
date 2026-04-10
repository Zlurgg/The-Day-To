package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
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
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<NotificationSettings, DataError.Local> {
        val userId = authRepository.getSignedInUser()?.userId ?: ANONYMOUS_USER_ID
        return when (val result = settingsRepository.getSettings(userId)) {
            is Result.Success -> Result.Success(result.data ?: NotificationSettings())
            is Result.Error -> result
        }
    }
}
