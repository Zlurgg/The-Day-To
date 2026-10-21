package uk.co.zlurgg.thedayto.notification.data.scheduler

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckTodayEntryExistsUseCase
import uk.co.zlurgg.thedayto.fake.FakeAuthRepository
import uk.co.zlurgg.thedayto.fake.FakeNotificationSettingsRepository
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID
import uk.co.zlurgg.thedayto.notification.data.worker.NotificationWorker
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings
import uk.co.zlurgg.thedayto.testutil.FakeTimeProvider
import java.util.concurrent.TimeUnit

/**
 * Unit tests for [NotificationSchedulerImpl.scheduleNextNotification] — the call the
 * worker uses to continue the daily chain.
 *
 * Key guarantee: a failure returns false WITHOUT cancelling the unique work, so the
 * running worker's retry is not cancelled out from under it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class NotificationSchedulerImplTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var settingsRepository: FakeNotificationSettingsRepository
    private lateinit var scheduler: NotificationSchedulerImpl

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build(),
        )
        workManager = WorkManager.getInstance(context)
        settingsRepository = FakeNotificationSettingsRepository()
        scheduler = NotificationSchedulerImpl(
            context = context,
            settingsRepository = settingsRepository,
            authRepository = FakeAuthRepository(),
            checkTodayEntryExists = object : CheckTodayEntryExistsUseCase {
                override suspend fun invoke(): Boolean = false
            },
            timeProvider = FakeTimeProvider(),
        )
    }

    /** Stands in for the already-queued chain (e.g. the currently running fire). */
    private fun enqueueExistingWork() {
        val request = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInitialDelay(1, TimeUnit.DAYS)
            .build()
        workManager.enqueueUniqueWork(NotificationWorker.NOTIFICATION_WORK, ExistingWorkPolicy.REPLACE, request)
            .result.get()
    }

    private fun uniqueWorkStates(): List<WorkInfo.State> =
        workManager.getWorkInfosForUniqueWork(NotificationWorker.NOTIFICATION_WORK).get().map { it.state }

    @Test
    fun `returns true and enqueues work when notifications are enabled`() = runTest {
        settingsRepository.saveSettings(ANONYMOUS_USER_ID, NotificationSettings(enabled = true, hour = 9, minute = 0))

        val result = scheduler.scheduleNextNotification()

        assertTrue(result)
        assertEquals(listOf(WorkInfo.State.ENQUEUED), uniqueWorkStates())
    }

    @Test
    fun `returns true and cancels the chain when notifications are disabled`() = runTest {
        enqueueExistingWork()
        settingsRepository.saveSettings(ANONYMOUS_USER_ID, NotificationSettings(enabled = false))

        val result = scheduler.scheduleNextNotification()

        assertTrue(result)
        assertTrue(uniqueWorkStates().all { it == WorkInfo.State.CANCELLED })
    }

    @Test
    fun `returns false and does not cancel existing work when settings cannot be read`() = runTest {
        enqueueExistingWork()
        settingsRepository.shouldReturnError = true

        val result = scheduler.scheduleNextNotification()

        assertFalse(result)
        assertEquals(listOf(WorkInfo.State.ENQUEUED), uniqueWorkStates())
    }
}
