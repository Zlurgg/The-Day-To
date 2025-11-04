package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import uk.co.zlurgg.thedayto.core.domain.model.NotificationSettings
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

/**
 * Gets notification settings from preferences.
 */
class GetNotificationSettingsUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(): NotificationSettings {
        return NotificationSettings(
            enabled = preferencesRepository.isNotificationEnabled(),
            hour = preferencesRepository.getNotificationHour(),
            minute = preferencesRepository.getNotificationMinute()
        )
    }
}
