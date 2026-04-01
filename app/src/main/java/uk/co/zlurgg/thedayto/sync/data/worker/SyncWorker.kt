package uk.co.zlurgg.thedayto.sync.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.sync.domain.usecase.PerformSyncUseCase

/**
 * WorkManager worker for performing background sync.
 *
 * Syncs local data with Firestore when:
 * - Periodic sync interval triggers
 * - Network becomes available after being offline
 * - App requests immediate sync
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val performSyncUseCase: PerformSyncUseCase by inject()

    override suspend fun doWork(): Result {
        Timber.d("SyncWorker: Starting sync")

        return when (val result = performSyncUseCase()) {
            is uk.co.zlurgg.thedayto.core.domain.result.Result.Success -> {
                val syncResult = result.data
                Timber.i(
                    "SyncWorker: Sync completed - uploaded: %d entries, %d moods; " +
                        "downloaded: %d entries, %d moods; conflicts: %d",
                    syncResult.entriesUploaded,
                    syncResult.moodColorsUploaded,
                    syncResult.entriesDownloaded,
                    syncResult.moodColorsDownloaded,
                    syncResult.conflictsResolved
                )
                Result.success()
            }
            is uk.co.zlurgg.thedayto.core.domain.result.Result.Error -> {
                Timber.w("SyncWorker: Sync failed with error: %s", result.error)
                // Retry on network errors, fail on auth/permission errors
                when (result.error) {
                    uk.co.zlurgg.thedayto.core.domain.error.DataError.Sync.NETWORK_ERROR -> {
                        Timber.d("SyncWorker: Will retry due to network error")
                        Result.retry()
                    }
                    uk.co.zlurgg.thedayto.core.domain.error.DataError.Sync.NOT_AUTHENTICATED,
                    uk.co.zlurgg.thedayto.core.domain.error.DataError.Sync.SYNC_DISABLED -> {
                        Timber.d("SyncWorker: Auth/disabled - not retrying")
                        Result.success() // Don't retry if not authenticated or disabled
                    }
                    else -> {
                        Timber.d("SyncWorker: Other error - will retry")
                        Result.retry()
                    }
                }
            }
        }
    }

    companion object {
        const val WORK_NAME_PERIODIC = "sync_periodic"
        const val WORK_NAME_IMMEDIATE = "sync_immediate"
    }
}
