package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings
import uk.co.zlurgg.thedayto.notification.domain.repository.NotificationSettingsRepository
import uk.co.zlurgg.thedayto.notification.domain.scheduler.NotificationScheduler

/**
 * Saves notification settings to Room and updates notification schedule.
 *
 * Saves settings for the current user (signed-in or anonymous).
 * Also schedules/cancels notifications based on the enabled flag.
 */
class SaveNotificationSettingsUseCase(
    private val settingsRepository: NotificationSettingsRepository,
    private val scheduler: NotificationScheduler,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(enabled: Boolean, hour: Int, minute: Int): EmptyResult<DataError.Local> {
        require(hour in MIN_HOUR..MAX_HOUR) { "Hour must be $MIN_HOUR-$MAX_HOUR, got: $hour" }
        require(minute in MIN_MINUTE..MAX_MINUTE) { "Minute must be $MIN_MINUTE-$MAX_MINUTE, got: $minute" }

        val userId = authRepository.getSignedInUser()?.userId ?: ANONYMOUS_USER_ID
        val settings = NotificationSettings(enabled = enabled, hour = hour, minute = minute)

        // Save settings to Room
        val result = settingsRepository.saveSettings(userId, settings)
        if (result is Result.Error) {
            return result
        }

        // Update notification schedule
        if (enabled) {
            scheduler.updateNotificationTime(hour, minute)
        } else {
            scheduler.cancelNotifications()
        }

        return Result.Success(Unit)
    }

    companion object {
        private const val MIN_HOUR = 0
        private const val MAX_HOUR = 23
        private const val MIN_MINUTE = 0
        private const val MAX_MINUTE = 59
    }
}
