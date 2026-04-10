package uk.co.zlurgg.thedayto.sync.data.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Scheduler for managing sync work requests.
 *
 * Handles:
 * - Periodic background sync (every 15 minutes when network available)
 * - Immediate sync requests (on app resume, data changes)
 * - Network restore sync (when connectivity returns)
 */
class SyncScheduler(
    private val context: Context,
) {
    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    /**
     * Start periodic background sync.
     * Called when sync is enabled.
     */
    fun startPeriodicSync() {
        Timber.d("SyncScheduler: Starting periodic sync")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            PERIODIC_SYNC_INTERVAL_MINUTES,
            TimeUnit.MINUTES,
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_MINUTES,
                TimeUnit.MINUTES,
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP, // Don't restart if already scheduled
            periodicWorkRequest,
        )

        Timber.i("SyncScheduler: Periodic sync scheduled every %d minutes", PERIODIC_SYNC_INTERVAL_MINUTES)
    }

    /**
     * Stop periodic background sync.
     * Called when sync is disabled or user signs out.
     */
    fun stopPeriodicSync() {
        Timber.d("SyncScheduler: Stopping periodic sync")
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME_PERIODIC)
        Timber.i("SyncScheduler: Periodic sync cancelled")
    }

    /**
     * Request an immediate sync.
     * Called when app comes to foreground or data changes.
     */
    fun requestImmediateSync() {
        Timber.d("SyncScheduler: Requesting immediate sync")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_MINUTES,
                TimeUnit.MINUTES,
            )
            .build()

        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME_IMMEDIATE,
            ExistingWorkPolicy.KEEP, // Don't cancel in-progress sync
            immediateWorkRequest,
        )

        Timber.i("SyncScheduler: Immediate sync enqueued")
    }

    /**
     * Cancel any pending immediate sync.
     */
    fun cancelImmediateSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME_IMMEDIATE)
    }

    /**
     * Cancel all sync work.
     */
    fun cancelAllSync() {
        Timber.d("SyncScheduler: Cancelling all sync work")
        stopPeriodicSync()
        cancelImmediateSync()
        Timber.i("SyncScheduler: All sync work cancelled")
    }

    companion object {
        // WorkManager minimum periodic interval is 15 minutes
        private const val PERIODIC_SYNC_INTERVAL_MINUTES = 15L
        private const val BACKOFF_DELAY_MINUTES = 1L
    }
}
