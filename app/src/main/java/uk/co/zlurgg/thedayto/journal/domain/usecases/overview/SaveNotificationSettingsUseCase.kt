package uk.co.zlurgg.thedayto.journal.domain.usecases.overview

import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository
import uk.co.zlurgg.thedayto.journal.domain.repository.PreferencesRepository

/**
 * Saves notification settings and updates notification schedule.
 */
class SaveNotificationSettingsUseCase(
    private val preferencesRepository: PreferencesRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(enabled: Boolean, hour: Int, minute: Int) {
        // Save settings to preferences
        preferencesRepository.setNotificationEnabled(enabled)
        preferencesRepository.setNotificationTime(hour, minute)

        // Update notification schedule
        if (enabled) {
            notificationRepository.updateNotificationTime(hour, minute)
        } else {
            notificationRepository.cancelNotifications()
        }
    }
}
