package uk.co.zlurgg.thedayto.notification.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import uk.co.zlurgg.thedayto.notification.data.worker.RescheduleNotificationWorker

/**
 * BroadcastReceiver that handles timezone changes.
 *
 * When the device's timezone changes, scheduled notifications may fire at the wrong
 * time. This receiver enqueues a WorkManager job to recalculate and reschedule
 * notifications based on the new timezone.
 *
 * Uses WorkManager instead of inline coroutines to:
 * - Avoid leaking coroutine scopes (BroadcastReceiver has 10-second limit)
 * - Properly handle suspend functions (Room operations)
 * - Survive process death
 */
class TimezoneChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            Timber.d("Timezone changed, enqueueing reschedule work")

            val workRequest = OneTimeWorkRequestBuilder<RescheduleNotificationWorker>()
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }

    companion object {
        const val WORK_NAME = "reschedule_notification"
    }
}
