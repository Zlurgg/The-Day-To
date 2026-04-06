package uk.co.zlurgg.thedayto.notification.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakeNotificationScheduler
import uk.co.zlurgg.thedayto.fake.FakeNotificationSettingsRepository
import uk.co.zlurgg.thedayto.fake.FakeNotificationSyncService
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings

/**
 * Unit tests for NotificationAuthUseCase.
 *
 * Tests sign-in and sign-out notification settings handling:
 * - Sign-in with remote settings (restores from account)
 * - Sign-in with anonymous settings (migrates to user)
 * - Sign-in without any settings (no-op)
 * - Sign-out (cancels notifications and deletes settings)
 */
class NotificationAuthUseCaseTest {

    private lateinit var settingsRepository: FakeNotificationSettingsRepository
    private lateinit var notificationScheduler: FakeNotificationScheduler
    private lateinit var syncService: FakeNotificationSyncService
    private lateinit var useCase: NotificationAuthUseCase

    @Before
    fun setup() {
        settingsRepository = FakeNotificationSettingsRepository()
        notificationScheduler = FakeNotificationScheduler()
        syncService = FakeNotificationSyncService()
        useCase = NotificationAuthUseCase(
            settingsRepository = settingsRepository,
            notificationScheduler = notificationScheduler,
            syncService = syncService
        )
    }

    // ============================================================
    // Sign-In Tests - Remote Settings (Account Priority)
    // ============================================================

    @Test
    fun `handleSignInSuccess - restores settings from account when remote exists`() = runTest {
        // Given: Remote settings exist for user
        val remoteSettings = NotificationSettings(enabled = true, hour = 10, minute = 30)
        syncService.setRemoteSettings("firebase_user_123", remoteSettings)

        // When: User signs in
        val result = useCase.handleSignInSuccess("firebase_user_123")

        // Then: Returns restored from account
        assertEquals(SignInNotificationResult.RestoredFromAccount, result)
    }

    @Test
    fun `handleSignInSuccess - reschedules notifications when remote settings enabled`() = runTest {
        // Given: Remote settings with notifications enabled
        syncService.setRemoteSettings(
            "firebase_user_123",
            NotificationSettings(enabled = true, hour = 14, minute = 45)
        )

        // When: User signs in
        useCase.handleSignInSuccess("firebase_user_123")

        // Then: Notifications are rescheduled to remote time
        assertTrue(
            "Notification should be scheduled at 14:45",
            notificationScheduler.isScheduledAt(14, 45)
        )
    }

    @Test
    fun `handleSignInSuccess - remote settings take priority over anonymous settings`() = runTest {
        // Given: Both remote and anonymous settings exist
        syncService.setRemoteSettings(
            "firebase_user_123",
            NotificationSettings(enabled = true, hour = 10, minute = 0)
        )
        settingsRepository.setSettings(
            ANONYMOUS_USER_ID,
            NotificationSettings(enabled = true, hour = 8, minute = 30)
        )

        // When: User signs in
        val result = useCase.handleSignInSuccess("firebase_user_123")

        // Then: Returns restored from account (remote wins)
        assertEquals(SignInNotificationResult.RestoredFromAccount, result)

        // And: Anonymous settings are deleted
        assertNull(settingsRepository.getSettingsDirectly(ANONYMOUS_USER_ID))

        // And: Scheduled at remote time, not anonymous time
        assertTrue(notificationScheduler.isScheduledAt(10, 0))
        assertFalse(notificationScheduler.isScheduledAt(8, 30))
    }

    @Test
    fun `handleSignInSuccess - deletes anonymous settings when remote exists`() = runTest {
        // Given: Both remote and anonymous settings exist
        syncService.setRemoteSettings(
            "firebase_user_123",
            NotificationSettings(enabled = true, hour = 10, minute = 0)
        )
        settingsRepository.setSettings(
            ANONYMOUS_USER_ID,
            NotificationSettings(enabled = true, hour = 8, minute = 30)
        )

        // When: User signs in
        useCase.handleSignInSuccess("firebase_user_123")

        // Then: Anonymous settings are deleted
        assertNull(settingsRepository.getSettingsDirectly(ANONYMOUS_USER_ID))
    }

    // ============================================================
    // Sign-In Tests - Anonymous Migration (No Remote)
    // ============================================================

    @Test
    fun `handleSignInSuccess - migrates anonymous settings when no remote exists`() = runTest {
        // Given: Anonymous user has settings, no remote settings
        val anonymousSettings = NotificationSettings(enabled = true, hour = 8, minute = 30)
        settingsRepository.setSettings(ANONYMOUS_USER_ID, anonymousSettings)
        // syncService has no remote settings (default)

        // When: User signs in
        val result = useCase.handleSignInSuccess("firebase_user_123")

        // Then: Settings are migrated to user
        assertEquals(SignInNotificationResult.MigratedAnonymous, result)

        val userSettings = settingsRepository.getSettingsDirectly("firebase_user_123")
        assertEquals(anonymousSettings.enabled, userSettings?.enabled)
        assertEquals(anonymousSettings.hour, userSettings?.hour)
        assertEquals(anonymousSettings.minute, userSettings?.minute)
    }

    @Test
    fun `handleSignInSuccess - deletes anonymous settings after migration`() = runTest {
        // Given: Anonymous user has notification settings
        settingsRepository.setSettings(
            ANONYMOUS_USER_ID,
            NotificationSettings(enabled = true, hour = 10, minute = 0)
        )

        // When: User signs in
        useCase.handleSignInSuccess("firebase_user_123")

        // Then: Anonymous settings are deleted
        assertNull(settingsRepository.getSettingsDirectly(ANONYMOUS_USER_ID))
    }

    @Test
    fun `handleSignInSuccess - reschedules notifications when enabled`() = runTest {
        // Given: Anonymous settings with notifications enabled
        settingsRepository.setSettings(
            ANONYMOUS_USER_ID,
            NotificationSettings(enabled = true, hour = 14, minute = 45)
        )

        // When: User signs in
        useCase.handleSignInSuccess("firebase_user_123")

        // Then: Notifications are rescheduled
        assertTrue(
            "Notification should be scheduled at 14:45",
            notificationScheduler.isScheduledAt(14, 45)
        )
    }

    @Test
    fun `handleSignInSuccess - does not reschedule when notifications disabled`() = runTest {
        // Given: Anonymous settings with notifications disabled
        settingsRepository.setSettings(
            ANONYMOUS_USER_ID,
            NotificationSettings(enabled = false, hour = 9, minute = 0)
        )

        // When: User signs in
        useCase.handleSignInSuccess("firebase_user_123")

        // Then: Notifications are not scheduled
        assertFalse(
            "Notification should not be scheduled",
            notificationScheduler.isScheduledAt(9, 0)
        )
    }

    @Test
    fun `handleSignInSuccess - returns NoSettingsFound when no anonymous settings`() = runTest {
        // Given: No anonymous settings exist

        // When: User signs in
        val result = useCase.handleSignInSuccess("firebase_user_123")

        // Then: Returns no settings found
        assertEquals(SignInNotificationResult.NoSettingsFound, result)
    }

    @Test
    fun `handleSignInSuccess - does not create user settings when no anonymous settings`() = runTest {
        // Given: No anonymous settings exist

        // When: User signs in
        useCase.handleSignInSuccess("firebase_user_123")

        // Then: No settings created for user
        assertNull(settingsRepository.getSettingsDirectly("firebase_user_123"))
    }

    // ============================================================
    // Sign-Out Tests
    // ============================================================

    @Test
    fun `handleSignOut - copies enabled settings to anonymous and reschedules`() = runTest {
        // Given: User has notification settings enabled
        settingsRepository.setSettings(
            "firebase_user_123",
            NotificationSettings(enabled = true, hour = 8, minute = 0)
        )

        // When: User signs out
        useCase.handleSignOut("firebase_user_123")

        // Then: Settings copied to anonymous and rescheduled
        val anonymousSettings = settingsRepository.getSettingsDirectly(ANONYMOUS_USER_ID)
        assertEquals(true, anonymousSettings?.enabled)
        assertEquals(8, anonymousSettings?.hour)
        assertEquals(0, anonymousSettings?.minute)
        assertTrue(notificationScheduler.isScheduledAt(8, 0))
    }

    @Test
    fun `handleSignOut - copies disabled settings to anonymous and cancels`() = runTest {
        // Given: User has notification settings disabled
        settingsRepository.setSettings(
            "firebase_user_123",
            NotificationSettings(enabled = false, hour = 10, minute = 30)
        )

        // When: User signs out
        useCase.handleSignOut("firebase_user_123")

        // Then: Settings copied to anonymous but notifications cancelled
        val anonymousSettings = settingsRepository.getSettingsDirectly(ANONYMOUS_USER_ID)
        assertEquals(false, anonymousSettings?.enabled)
        assertTrue(notificationScheduler.cancelNotificationsCalled)
    }

    @Test
    fun `handleSignOut - deletes user notification settings`() = runTest {
        // Given: User has notification settings
        settingsRepository.setSettings(
            "firebase_user_123",
            NotificationSettings(enabled = true, hour = 10, minute = 30)
        )

        // When: User signs out
        useCase.handleSignOut("firebase_user_123")

        // Then: Account settings are deleted
        assertNull(settingsRepository.getSettingsDirectly("firebase_user_123"))
    }

    @Test
    fun `handleSignOut - cancels when no settings exist`() = runTest {
        // Given: No settings for user

        // When: User signs out
        useCase.handleSignOut("firebase_user_123")

        // Then: No exception thrown, notifications cancelled
        assertTrue(notificationScheduler.cancelNotificationsCalled)
        assertNull(settingsRepository.getSettingsDirectly(ANONYMOUS_USER_ID))
    }

    @Test
    fun `handleSignOut - does not affect other users`() = runTest {
        // Given: Multiple users have settings
        settingsRepository.setSettings(
            "user_A",
            NotificationSettings(enabled = true, hour = 8, minute = 0)
        )
        settingsRepository.setSettings(
            "user_B",
            NotificationSettings(enabled = true, hour = 9, minute = 0)
        )

        // When: User A signs out
        useCase.handleSignOut("user_A")

        // Then: User A settings deleted, User B settings remain
        assertNull(settingsRepository.getSettingsDirectly("user_A"))
        assertEquals(9, settingsRepository.getSettingsDirectly("user_B")?.hour)
        // And: Anonymous gets User A's settings
        assertEquals(8, settingsRepository.getSettingsDirectly(ANONYMOUS_USER_ID)?.hour)
    }

    // ============================================================
    // Edge Case Tests
    // ============================================================

    @Test
    fun `handleSignInSuccess - restores remote settings after previous sign-out`() = runTest {
        // Given: User had settings from previous sign-in (now in Firestore)
        // And: New anonymous settings exist
        syncService.setRemoteSettings(
            "firebase_user_456",
            NotificationSettings(enabled = true, hour = 10, minute = 0)
        )
        settingsRepository.setSettings(
            ANONYMOUS_USER_ID,
            NotificationSettings(enabled = true, hour = 7, minute = 15)
        )

        // When: User signs in again
        val result = useCase.handleSignInSuccess("firebase_user_456")

        // Then: Remote settings restored (not anonymous)
        assertEquals(SignInNotificationResult.RestoredFromAccount, result)
        assertTrue(notificationScheduler.isScheduledAt(10, 0))
    }

    @Test
    fun `handleSignInSuccess - adopts anonymous when no prior account settings`() = runTest {
        // Given: New user with anonymous settings, no remote settings
        settingsRepository.setSettings(
            ANONYMOUS_USER_ID,
            NotificationSettings(enabled = true, hour = 7, minute = 15)
        )

        // When: User signs in
        val result = useCase.handleSignInSuccess("firebase_user_456")

        // Then: Settings migrated successfully
        assertEquals(SignInNotificationResult.MigratedAnonymous, result)
        assertEquals(7, settingsRepository.getSettingsDirectly("firebase_user_456")?.hour)
        assertEquals(15, settingsRepository.getSettingsDirectly("firebase_user_456")?.minute)
    }
}
