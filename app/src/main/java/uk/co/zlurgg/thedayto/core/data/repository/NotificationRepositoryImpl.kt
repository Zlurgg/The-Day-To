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
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.entry.GetEntryByDateUseCase
import kotlinx.coroutines.runBlocking
import uk.co.zlurgg.thedayto.core.service.notifications.NotificationWorker
import uk.co.zlurgg.thedayto.core.service.notifications.NotificationWorker.Companion.NOTIFICATION_ID
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

/**
 * Implementation of NotificationRepository.
 *
 * Handles notification scheduling using WorkManager and checks user's
 * entry status by querying the database directly.
 *
 * Follows Clean Architecture:
 * - Uses dependency injection (Context + GetEntryByDateUseCase via Koin)
 * - Single source of truth: Database
 * - Implements domain layer interface
 *
 * @param context Application context for WorkManager and permission checks
 * @param getEntryByDateUseCase Use case for checking if entry exists for a date
 */
class NotificationRepositoryImpl(
    private val context: Context,
    private val getEntryByDateUseCase: GetEntryByDateUseCase
) : NotificationRepository {

    override fun setupDailyNotificationIfNeeded() {
        try {
            val yesterday = LocalDate.now()
                .atStartOfDay()
                .minusDays(1)
                .toEpochSecond(ZoneOffset.UTC)

            // Check if entry exists for yesterday using database
            val yesterdayEntry = runBlocking {
                getEntryByDateUseCase(yesterday)
            }

            // Schedule notification if:
            // 1. User made entry yesterday (needs reminder for today)
            // 2. First-time user (no entry for yesterday - fresh install)
            if (yesterdayEntry != null) {
                scheduleNotification()
                Timber.d("Notification scheduled - User created entry yesterday")
            } else {
                Timber.d("Notification NOT needed - No entry yesterday")
            }
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

    /**
     * Schedules a notification for the next day using WorkManager.
     *
     * - Uses REPLACE policy to prevent duplicate notifications
     * - NO network constraint (notifications don't need internet)
     * - Calculates delay until tomorrow
     */
    private fun scheduleNotification() {
        try {
            // Calculate delay until tomorrow (next day at start of day)
            val userNotificationTime = LocalDateTime.now()
                .plusDays(1)
                .toEpochSecond(ZoneOffset.UTC)

            val currentTime = LocalDateTime.now()
                .toEpochSecond(ZoneOffset.UTC)

            val delay = userNotificationTime - currentTime

            // Create input data for worker
            val data = Data.Builder()
                .putInt(NOTIFICATION_ID, 0)
                .build()

            // Create constraints (NO network required - fixed from original)
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

            Timber.i("Notification scheduled successfully for $delay seconds from now")
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule notification")
        }
    }
}
