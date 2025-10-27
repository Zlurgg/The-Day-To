package uk.co.zlurgg.thedayto.core.data.repository

import android.content.Context
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import uk.co.zlurgg.thedayto.fake.FakePreferencesRepository
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for NotificationRepositoryImpl.
 *
 * Uses Robolectric to provide Android context without needing an emulator.
 * Uses WorkManagerTestInitHelper for testing WorkManager scheduling.
 * Uses FakePreferencesRepository to control test state without SharedPreferences.
 *
 * TDD Approach: These tests are written BEFORE the implementation.
 */
@RunWith(RobolectricTestRunner::class)
class NotificationRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var notificationRepository: NotificationRepositoryImpl
    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        // Initialize Robolectric context
        context = RuntimeEnvironment.getApplication()

        // Initialize WorkManager for testing
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        workManager = WorkManager.getInstance(context)

        // Create fake repository with controlled state
        fakePreferencesRepository = FakePreferencesRepository()

        // Create system under test
        notificationRepository = NotificationRepositoryImpl(
            context = context,
            preferencesRepository = fakePreferencesRepository
        )
    }

    @After
    fun tearDown() {
        // Reset fake repository state
        fakePreferencesRepository.reset()

        // Cancel all work to clean up
        workManager.cancelAllWork()
    }

    /**
     * Test: Should schedule notification when user made entry yesterday.
     *
     * Scenario: User logged mood yesterday, should be reminded tomorrow.
     */
    @Test
    fun `setupDailyNotificationIfNeeded schedules notification when entry from yesterday`() {
        // Given: User made entry yesterday
        val yesterday = LocalDate.now()
            .atStartOfDay()
            .minusDays(1)
            .toEpochSecond(ZoneOffset.UTC)
        fakePreferencesRepository.setEntryDate(yesterday)

        // When: Setup notification
        notificationRepository.setupDailyNotificationIfNeeded()

        // Then: Notification should be scheduled
        val workInfos = workManager.getWorkInfosForUniqueWork(
            "thedayto_notification_work"
        ).get()

        assertEquals(1, workInfos.size, "Should have one scheduled work")
        assertNotNull(workInfos.firstOrNull(), "Work should be scheduled")
    }

    /**
     * Test: Should schedule notification for first-time user.
     *
     * Scenario: New user (entryDate = 0) should get welcome notification.
     */
    @Test
    fun `setupDailyNotificationIfNeeded schedules notification for first time user`() {
        // Given: First time user (entry date = 0)
        fakePreferencesRepository.setEntryDate(0L)

        // When: Setup notification
        notificationRepository.setupDailyNotificationIfNeeded()

        // Then: Notification should be scheduled
        val workInfos = workManager.getWorkInfosForUniqueWork(
            "thedayto_notification_work"
        ).get()

        assertEquals(1, workInfos.size, "Should schedule notification for first-time user")
    }

    /**
     * Test: Should NOT schedule when entry already made today.
     *
     * Scenario: User already logged mood today, no reminder needed.
     */
    @Test
    fun `setupDailyNotificationIfNeeded does NOT schedule when entry made today`() {
        // Given: User made entry today
        val today = LocalDate.now()
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC)
        fakePreferencesRepository.setEntryDate(today)

        // When: Setup notification
        notificationRepository.setupDailyNotificationIfNeeded()

        // Then: No notification should be scheduled
        val workInfos = workManager.getWorkInfosForUniqueWork(
            "thedayto_notification_work"
        ).get()

        assertEquals(0, workInfos.size, "Should NOT schedule when entry already made today")
    }

    /**
     * Test: Should NOT schedule when entry made in the future.
     *
     * Edge case: Prevent scheduling if date is somehow in the future.
     */
    @Test
    fun `setupDailyNotificationIfNeeded does NOT schedule when entry made in future`() {
        // Given: Entry date in the future (edge case)
        val tomorrow = LocalDate.now()
            .atStartOfDay()
            .plusDays(1)
            .toEpochSecond(ZoneOffset.UTC)
        fakePreferencesRepository.setEntryDate(tomorrow)

        // When: Setup notification
        notificationRepository.setupDailyNotificationIfNeeded()

        // Then: No notification should be scheduled
        val workInfos = workManager.getWorkInfosForUniqueWork(
            "thedayto_notification_work"
        ).get()

        assertEquals(0, workInfos.size, "Should NOT schedule when entry is in the future")
    }

    /**
     * Test: Should cancel existing scheduled notifications.
     *
     * Scenario: User signs out or disables notifications.
     */
    @Test
    fun `cancelNotifications cancels all scheduled work`() {
        // Given: Notification is scheduled
        fakePreferencesRepository.setEntryDate(0L)
        notificationRepository.setupDailyNotificationIfNeeded()

        // Verify it's scheduled
        var workInfos = workManager.getWorkInfosForUniqueWork(
            "thedayto_notification_work"
        ).get()
        assertEquals(1, workInfos.size, "Notification should be scheduled initially")

        // When: Cancel notifications
        notificationRepository.cancelNotifications()

        // Then: Work should be cancelled
        workInfos = workManager.getWorkInfosForUniqueWork(
            "thedayto_notification_work"
        ).get()
        assertEquals(0, workInfos.size, "All notifications should be cancelled")
    }

    /**
     * Test: Should replace existing notification when called multiple times.
     *
     * Scenario: Prevents duplicate notifications.
     */
    @Test
    fun `setupDailyNotificationIfNeeded replaces existing work`() {
        // Given: Notification already scheduled
        fakePreferencesRepository.setEntryDate(0L)
        notificationRepository.setupDailyNotificationIfNeeded()

        // When: Setup called again
        notificationRepository.setupDailyNotificationIfNeeded()

        // Then: Should still only have ONE scheduled work (replaced, not added)
        val workInfos = workManager.getWorkInfosForUniqueWork(
            "thedayto_notification_work"
        ).get()

        assertEquals(1, workInfos.size, "Should replace existing work, not create duplicate")
    }

    // Note: hasNotificationPermission() tests are commented out because they depend on
    // Robolectric API level configuration and require additional setup.
    // In a real app, you might use:
    // @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    // to test API 33+ behavior
}
