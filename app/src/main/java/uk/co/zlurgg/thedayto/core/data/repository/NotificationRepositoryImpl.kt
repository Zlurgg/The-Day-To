package uk.co.zlurgg.thedayto.core.data.repository

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.data.service.notifications.NotificationWorker
import uk.co.zlurgg.thedayto.core.data.service.notifications.NotificationWorker.Companion.NOTIFICATION_ID
import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckTodayEntryExistsUseCase
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID
import uk.co.zlurgg.thedayto.notification.domain.repository.NotificationSettingsRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Implementation of NotificationRepository (scheduler).
 *
 * Handles notification scheduling using WorkManager based on user settings.
 *
 * Follows Clean Architecture:
 * - Uses dependency injection via Koin
 * - Single source of truth: NotificationSettingsRepository (Room) for settings
 * - Implements domain layer interface
 *
 * @param context Application context for WorkManager and permission checks
 * @param settingsRepository Repository for reading notification settings
 * @param authRepository Repository for getting current user
 * @param checkTodayEntryExists Use case to check if today's entry exists
 */
class NotificationRepositoryImpl(
    private val context: Context,
    private val settingsRepository: NotificationSettingsRepository,
    private val authRepository: AuthRepository,
    private val checkTodayEntryExists: CheckTodayEntryExistsUseCase
) : NotificationRepository {

    // Repository-level coroutine scope for background operations
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun setupDailyNotification() {
        Timber.d("setupDailyNotification called - checking settings")
        // Launch in repository scope to avoid blocking
        repositoryScope.launch {
            try {
                val userId = authRepository.getSignedInUser()?.userId ?: ANONYMOUS_USER_ID
                val settings = settingsRepository.getSettings(userId)

                if (settings == null || !settings.enabled) {
                    Timber.d("Notifications disabled or not configured - skipping schedule")
                    cancelNotifications()
                    return@launch
                }

                Timber.d("Notifications enabled: hour=%d, minute=%d", settings.hour, settings.minute)
                scheduleNotification(settings.hour, settings.minute)
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
        // Schedule notification at the specified time
        // Note: Settings are saved by SaveNotificationSettingsUseCase before calling this
        repositoryScope.launch {
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

    override fun areSystemNotificationsEnabled(): Boolean {
        return try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.areNotificationsEnabled()
        } catch (e: Exception) {
            Timber.e(e, "Error checking system notification status")
            // Fail-safe: assume enabled if we can't determine
            true
        }
    }

    override fun shouldShowPermissionRationale(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // No runtime permission needed before Android 13
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
            // Fail-safe: assume we should not show rationale if we can't determine
            false
        }
    }

    /**
     * Schedules a periodic daily notification using WorkManager.
     *
     * Uses PeriodicWorkRequest with 24-hour interval for reliable daily notifications.
     * - UPDATE policy: reschedules with new timing if settings change
     * - NO network constraint (notifications don't need internet)
     * - Initial delay calculated to first occurrence of specified time
     *
     * @param hour hour in 24-hour format (0-23)
     * @param minute minute (0-59)
     */
    private fun scheduleNotification(hour: Int, minute: Int) {
        try {
            Timber.d("Scheduling periodic notification for $hour:${minute.toString().padStart(2, '0')}")

            // Calculate delay until first notification (system timezone)
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
            val initialDelay = nextEpoch - currentEpoch

            Timber.d("Initial delay: ${initialDelay}s (${initialDelay / 3600}h ${(initialDelay % 3600) / 60}m)")

            // Create input data for worker
            val data = Data.Builder()
                .putInt(NOTIFICATION_ID, 0)
                .build()

            // Create constraints (NO network required)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            // Create periodic work request (24-hour interval)
            val notificationWorker = PeriodicWorkRequest.Builder(
                NotificationWorker::class.java,
                24, TimeUnit.HOURS
            )
                .setInputData(data)
                .setInitialDelay(initialDelay, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build()

            // Schedule periodic work (UPDATE policy reschedules with new timing)
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NotificationWorker.NOTIFICATION_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                notificationWorker
            )

            Timber.i("Periodic notification scheduled: first in ${initialDelay}s, then every 24h at $hour:${minute.toString().padStart(2, '0')}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule periodic notification")
        }
    }
}
