package uk.co.zlurgg.thedayto.core.service.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.service.notifications.NotificationWorker.Companion.NOTIFICATION_ID
import uk.co.zlurgg.thedayto.core.service.notifications.NotificationWorker.Companion.NOTIFICATION_WORK
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

/**
 * Notification initialization helper.
 *
 * Follows Google's "Now in Android" pattern:
 * - Called once from Application.onCreate()
 * - Schedules unique work with REPLACE policy
 * - WorkManager handles persistence and rescheduling
 *
 * This is the single entry point for notification scheduling.
 */
object NotificationScheduler {

    /**
     * Initialize daily notification scheduling.
     *
     * Should be called from Application.onCreate() to ensure
     * notifications are scheduled when the app starts.
     *
     * Uses ExistingWorkPolicy.REPLACE to always update the schedule
     * with fresh timing calculations.
     *
     * @param context Application context
     */
    fun initialize(context: Context) {
        try {
            Timber.d("Initializing notification scheduler")

            // Calculate delay until next day at start of day
            val nextNotificationTime = LocalDateTime.now()
                .plusDays(1)
                .withHour(9)  // 9 AM notification time
                .withMinute(0)
                .withSecond(0)
                .toEpochSecond(ZoneOffset.UTC)

            val currentTime = LocalDateTime.now()
                .toEpochSecond(ZoneOffset.UTC)

            val delay = nextNotificationTime - currentTime

            // Create input data for worker
            val data = Data.Builder()
                .putInt(NOTIFICATION_ID, 0)
                .build()

            // Create constraints (no network required for notifications)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            // Create one-time work request
            val notificationWork = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInputData(data)
                .setInitialDelay(delay, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build()

            // Enqueue unique work
            // REPLACE policy: Always update with fresh timing
            WorkManager.getInstance(context).enqueueUniqueWork(
                NOTIFICATION_WORK,
                ExistingWorkPolicy.REPLACE,
                notificationWork
            )

            Timber.i("Notification scheduled for ${delay}s from now (9 AM tomorrow)")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize notification scheduler")
        }
    }
}
