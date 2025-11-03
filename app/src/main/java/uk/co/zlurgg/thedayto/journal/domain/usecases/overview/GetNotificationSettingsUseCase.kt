package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.journal.domain.repository.PreferencesRepository

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

data class NotificationSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)
