package uk.co.zlurgg.thedayto.notification.data.scheduler

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
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
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckTodayEntryExistsUseCase
import uk.co.zlurgg.thedayto.core.domain.util.TimeProvider
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID
import uk.co.zlurgg.thedayto.notification.data.worker.NotificationWorker
import uk.co.zlurgg.thedayto.notification.data.worker.NotificationWorker.Companion.NOTIFICATION_ID
import uk.co.zlurgg.thedayto.notification.domain.repository.NotificationSettingsRepository
import uk.co.zlurgg.thedayto.notification.domain.scheduler.NotificationScheduler
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Implementation of NotificationScheduler.
 *
 * Handles notification scheduling using WorkManager based on user settings.
 *
 * @param context Application context for WorkManager and permission checks
 * @param settingsRepository Repository for reading notification settings
 * @param authRepository Repository for getting current user
 * @param checkTodayEntryExists Use case to check if today's entry exists
 * @param timeProvider Source of the current time (injectable for testability)
 */
class NotificationSchedulerImpl(
    private val context: Context,
    private val settingsRepository: NotificationSettingsRepository,
    private val authRepository: AuthRepository,
    private val checkTodayEntryExists: CheckTodayEntryExistsUseCase,
    private val timeProvider: TimeProvider,
) : NotificationScheduler {

    private val schedulerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun setupDailyNotification() {
        Timber.d("setupDailyNotification called - checking settings")
        schedulerScope.launch { scheduleFromSettings() }
    }

    override suspend fun scheduleNextNotification() {
        scheduleFromSettings()
    }

    /**
     * Reads the current notification settings and schedules the next single fire at the
     * stored time, or cancels if notifications are disabled / unconfigured.
     *
     * Shared by [setupDailyNotification] (app startup) and [scheduleNextNotification]
     * (the worker continuing the daily chain).
     */
    private suspend fun scheduleFromSettings() {
        try {
            val userId = authRepository.getSignedInUser()?.userId ?: ANONYMOUS_USER_ID
            val settings = when (val settingsResult = settingsRepository.getSettings(userId)) {
                is Result.Success -> settingsResult.data
                is Result.Error -> {
                    Timber.e("Failed to get notification settings: %s", settingsResult.error)
                    cancelNotifications()
                    return
                }
            }

            if (settings == null || !settings.enabled) {
                Timber.d("Notifications disabled or not configured - skipping schedule")
                cancelNotifications()
                return
            }

            Timber.d("Notifications enabled: hour=%d, minute=%d", settings.hour, settings.minute)
            scheduleNotification(settings.hour, settings.minute)
        } catch (e: Exception) {
            Timber.e(e, "Failed to setup daily notification")
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
        schedulerScope.launch {
            try {
                scheduleNotification(hour, minute)
                Timber.i("Notification rescheduled for %d:%02d", hour, minute)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update notification time")
            }
        }
    }

    override fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override suspend fun shouldSendNotification(): Boolean {
        return try {
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
            true
        }
    }

    override fun areSystemNotificationsEnabled(): Boolean {
        return try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.areNotificationsEnabled()
        } catch (e: Exception) {
            Timber.e(e, "Error checking system notification status")
            true
        }
    }

    override fun shouldShowPermissionRationale(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                return false
            }

            val activity = context as? Activity
            if (activity == null) {
                Timber.w("Context is not an Activity - cannot check permission rationale")
                return false
            }

            activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        } catch (e: Exception) {
            Timber.e(e, "Error checking permission rationale")
            false
        }
    }

    /**
     * Schedules a single notification at the given time using a one-time WorkManager
     * request. After it fires, [NotificationWorker] calls [scheduleNextNotification] to
     * enqueue the following day, always re-anchored to the stored time. This self-
     * rescheduling chain is what prevents the fire time from drifting later each day
     * (unlike a [androidx.work.PeriodicWorkRequest], whose window re-anchors to the last
     * actual — potentially Doze-deferred — run).
     */
    private fun scheduleNotification(hour: Int, minute: Int) {
        try {
            Timber.d(
                "Scheduling notification for %d:%s",
                hour,
                minute.toString().padStart(2, '0'),
            )

            val initialDelay = NotificationSchedule.initialDelaySeconds(
                now = timeProvider.now(),
                zone = ZoneId.systemDefault(),
                hour = hour,
                minute = minute,
            )

            Timber.d(
                "Initial delay: %ds (%dh %dm)",
                initialDelay,
                initialDelay / SECONDS_PER_HOUR,
                (initialDelay % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE,
            )

            val data = Data.Builder()
                .putInt(NOTIFICATION_ID, 0)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val notificationWorker = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInputData(data)
                .setInitialDelay(initialDelay, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                NotificationWorker.NOTIFICATION_WORK,
                ExistingWorkPolicy.REPLACE,
                notificationWorker,
            )

            Timber.i(
                "Notification scheduled: fires in %ds at %d:%s (re-scheduled after each fire)",
                initialDelay,
                hour,
                minute.toString().padStart(2, '0'),
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule notification")
        }
    }

    companion object {
        private const val SECONDS_PER_HOUR = 3600
        private const val SECONDS_PER_MINUTE = 60
    }
}
