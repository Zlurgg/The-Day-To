package uk.co.zlurgg.thedayto.notification.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID
import uk.co.zlurgg.thedayto.notification.domain.repository.NotificationSettingsRepository

/**
 * Worker that reschedules notifications after timezone change.
 *
 * Reads notification settings from Room and uses the existing NotificationRepository
 * (scheduler) to set up the notification at the correct time for the new timezone.
 *
 * Enqueued by [TimezoneChangeReceiver] when ACTION_TIMEZONE_CHANGED is received.
 */
class RescheduleNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val settingsRepository: NotificationSettingsRepository by inject()
    private val scheduler: NotificationRepository by inject()
    private val authRepository: AuthRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            val userId = authRepository.getSignedInUser()?.userId ?: ANONYMOUS_USER_ID
            val settings = settingsRepository.getSettings(userId)

            if (settings == null) {
                Timber.d("No notification settings for user %s, nothing to reschedule", userId)
                return Result.success()
            }

            if (!settings.enabled) {
                Timber.d("Notifications disabled for user %s, nothing to reschedule", userId)
                return Result.success()
            }

            Timber.d(
                "Rescheduling notification for %d:%02d after timezone change",
                settings.hour,
                settings.minute
            )

            // Use existing scheduler to reschedule
            scheduler.updateNotificationTime(settings.hour, settings.minute)

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Failed to reschedule notification")
            Result.failure()
        }
    }
}
