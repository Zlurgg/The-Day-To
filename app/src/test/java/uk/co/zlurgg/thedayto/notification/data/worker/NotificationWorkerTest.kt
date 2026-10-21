package uk.co.zlurgg.thedayto.notification.data.worker

import android.app.Application
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import uk.co.zlurgg.thedayto.fake.FakeNotificationScheduler
import uk.co.zlurgg.thedayto.notification.domain.scheduler.NotificationScheduler

/**
 * Unit tests for [NotificationWorker]'s chain-continuation behaviour.
 *
 * The worker re-schedules the next day's fire itself, so a failure to schedule must
 * produce [ListenableWorker.Result.retry] rather than silently ending the daily chain.
 *
 * [FakeNotificationScheduler.shouldSendNotification] is false throughout so no system
 * notification is posted; these tests are about scheduling, not notification content.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class NotificationWorkerTest {

    private lateinit var context: Context
    private lateinit var fakeScheduler: FakeNotificationScheduler

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        fakeScheduler = FakeNotificationScheduler().apply { shouldSendNotification = false }
        startKoin {
            modules(module { single<NotificationScheduler> { fakeScheduler } })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun runWorker(runAttemptCount: Int = 0): ListenableWorker.Result =
        TestListenableWorkerBuilder<NotificationWorker>(context)
            .setRunAttemptCount(runAttemptCount)
            .build()
            .doWork()

    @Test
    fun `schedules next notification and succeeds when scheduling succeeds`() {
        val result = runWorker()

        assertEquals(ListenableWorker.Result.success(), result)
        assertTrue(fakeScheduler.scheduleNextNotificationCalled)
    }

    @Test
    fun `retries when scheduling the next notification fails`() {
        fakeScheduler.scheduleNextNotificationSucceeds = false

        val result = runWorker()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `first attempt checks whether a reminder is needed`() {
        runWorker(runAttemptCount = 0)

        assertTrue(fakeScheduler.shouldSendNotificationCalled)
    }

    @Test
    fun `retry attempt only reschedules and does not re-check or re-post the reminder`() {
        val result = runWorker(runAttemptCount = 1)

        assertFalse(fakeScheduler.shouldSendNotificationCalled)
        assertTrue(fakeScheduler.scheduleNextNotificationCalled)
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `retry attempt keeps retrying while scheduling still fails`() {
        fakeScheduler.scheduleNextNotificationSucceeds = false

        val result = runWorker(runAttemptCount = 3)

        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
