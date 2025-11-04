package uk.co.zlurgg.thedayto.core.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.data.service.notifications.NotificationWorker
import uk.co.zlurgg.thedayto.core.data.service.notifications.NotificationWorker.Companion.NOTIFICATION_ID
import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckTodayEntryExistsUseCase
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Implementation of NotificationRepository.
 *
 * Handles notification scheduling using WorkManager based on user preferences.
 *
 * Follows Clean Architecture:
 * - Uses dependency injection (Context + PreferencesRepository via Koin)
 * - Single source of truth: PreferencesRepository for notification settings
 * - Implements domain layer interface
 * - Repository can use domain use cases (proper layer interaction)
 *
 * @param context Application context for WorkManager and permission checks
 * @param preferencesRepository Repository for reading/writing notification preferences
 * @param checkTodayEntryExists Use case to check if today's entry exists
 */
class NotificationRepositoryImpl(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val checkTodayEntryExists: CheckTodayEntryExistsUseCase
) : NotificationRepository {

    // Repository-level coroutine scope for background operations
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun setupDailyNotification() {
        // Launch in repository scope to avoid blocking
        repositoryScope.launch {
            try {
                // Check if notifications are enabled in preferences
                val isEnabled = preferencesRepository.isNotificationEnabled()

                if (!isEnabled) {
                    Timber.d("Notifications disabled in preferences - skipping schedule")
                    cancelNotifications()
                    return@launch
                }

                // Get user-configured notification time
                val hour = preferencesRepository.getNotificationHour()
                val minute = preferencesRepository.getNotificationMinute()

                scheduleNotification(hour, minute)
                Timber.d("Notification scheduled for $hour:${minute.toString().padStart(2, '0')}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to setup daily notification")
            }
        }
    }

    override fun cancelNotifications() {
        try {
            WorkManager.getInstance(context)
                .cancelUniqueWork(NotificationWorker.NOTIFICATION_WORK)
            Timber.i("Cancelled all scheduled notifications")
        } catch (e: Exception) {
            Timber.e(e, "Failed to cancel notifications")
        }
    }

    override fun updateNotificationTime(hour: Int, minute: Int) {
        repositoryScope.launch {
            try {
                // Save new time to preferences
                preferencesRepository.setNotificationTime(hour, minute)

                // Reschedule with new time (if notifications are enabled)
                val isEnabled = preferencesRepository.isNotificationEnabled()
                if (isEnabled) {
                    scheduleNotification(hour, minute)
                    Timber.i("Notification rescheduled for $hour:${minute.toString().padStart(2, '0')}")
                } else {
                    Timber.d("Notifications disabled - time updated but not scheduled")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update notification time")
            }
        }
    }

    override fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Pre-API 33: No runtime permission required
            true
        }
    }

    override suspend fun shouldSendNotification(): Boolean {
        return try {
            // Check if an entry already exists for today
            val hasEntryToday = checkTodayEntryExists()

            if (hasEntryToday) {
                Timber.d("Entry already exists for today - notification not needed")
                false
            } else {
                Timber.d("No entry for today - notification should be sent")
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking if notification should be sent")
            // Fail-safe: send notification if we can't determine entry status
            true
        }
    }

    /**
     * Schedules a notification for the specified time tomorrow using WorkManager.
     *
     * - Uses REPLACE policy to prevent duplicate notifications
     * - NO network constraint (notifications don't need internet)
     * - Calculates delay until specified time tomorrow in system timezone
     *
     * @param hour hour in 24-hour format (0-23)
     * @param minute minute (0-59)
     */
    private fun scheduleNotification(hour: Int, minute: Int) {
        try {
            // Calculate delay until specified time tomorrow (system timezone)
            val systemZone = ZoneId.systemDefault()
            val now = LocalDateTime.now(systemZone)

            // Calculate next notification time
            var nextNotificationTime = now
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0)

            // If the time has already passed today, schedule for tomorrow
            if (nextNotificationTime.isBefore(now) || nextNotificationTime.isEqual(now)) {
                nextNotificationTime = nextNotificationTime.plusDays(1)
            }

            val currentEpoch = now.atZone(systemZone).toEpochSecond()
            val nextEpoch = nextNotificationTime.atZone(systemZone).toEpochSecond()
            val delay = nextEpoch - currentEpoch

            // Create input data for worker
            val data = Data.Builder()
                .putInt(NOTIFICATION_ID, 0)
                .build()

            // Create constraints (NO network required)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            // Create one-time work request
            val notificationWorker = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInputData(data)
                .setInitialDelay(delay, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build()

            // Schedule work (REPLACE policy prevents duplicates)
            WorkManager.getInstance(context).beginUniqueWork(
                NotificationWorker.NOTIFICATION_WORK,
                ExistingWorkPolicy.REPLACE,
                notificationWorker
            ).enqueue()

            Timber.i("Notification scheduled for ${delay}s from now at $hour:${minute.toString().padStart(2, '0')}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule notification")
        }
    }
}
