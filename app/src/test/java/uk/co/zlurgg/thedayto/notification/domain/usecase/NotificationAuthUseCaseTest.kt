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
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings

/**
 * Unit tests for NotificationAuthUseCase.
 *
 * Tests sign-in and sign-out notification settings handling:
 * - Sign-in with anonymous settings (migrates to user)
 * - Sign-in without anonymous settings (no-op)
 * - Sign-out (cancels notifications and deletes settings)
 */
class NotificationAuthUseCaseTest {

    private lateinit var settingsRepository: FakeNotificationSettingsRepository
    private lateinit var notificationScheduler: FakeNotificationScheduler
    private lateinit var useCase: NotificationAuthUseCase

    @Before
    fun setup() {
        settingsRepository = FakeNotificationSettingsRepository()
        notificationScheduler = FakeNotificationScheduler()
        useCase = NotificationAuthUseCase(
            settingsRepository = settingsRepository,
            notificationScheduler = notificationScheduler
        )
    }

    // ============================================================
    // Sign-In Tests
    // ============================================================

    @Test
    fun `handleSignInSuccess - migrates anonymous settings to signed-in user`() = runTest {
        // Given: Anonymous user has notification settings
        val anonymousSettings = NotificationSettings(enabled = true, hour = 8, minute = 30)
        settingsRepository.setSettings(ANONYMOUS_USER_ID, anonymousSettings)

        // When: User signs in
        val result = useCase.handleSignInSuccess("firebase_user_123")

        // Then: Settings are migrated to user
        assertEquals(SignInNotificationResult.MigratedAnonymous, result)

        val userSettings = settingsRepository.getSettings("firebase_user_123")
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
        assertNull(settingsRepository.getSettings(ANONYMOUS_USER_ID))
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
        assertNull(settingsRepository.getSettings("firebase_user_123"))
    }

    // ============================================================
    // Sign-Out Tests
    // ============================================================

    @Test
    fun `handleSignOut - cancels scheduled notifications`() = runTest {
        // Given: User has notification settings enabled
        settingsRepository.setSettings(
            "firebase_user_123",
            NotificationSettings(enabled = true, hour = 8, minute = 0)
        )

        // When: User signs out
        useCase.handleSignOut("firebase_user_123")

        // Then: Notifications are cancelled
        assertTrue(
            "Notifications should be cancelled",
            notificationScheduler.cancelNotificationsCalled
        )
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

        // Then: Settings are deleted
        assertNull(settingsRepository.getSettings("firebase_user_123"))
    }

    @Test
    fun `handleSignOut - is safe when no settings exist`() = runTest {
        // Given: No settings for user

        // When: User signs out
        useCase.handleSignOut("firebase_user_123")

        // Then: No exception thrown, notifications cancelled as precaution
        assertTrue(
            "Notifications should be cancelled",
            notificationScheduler.cancelNotificationsCalled
        )
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
        assertNull(settingsRepository.getSettings("user_A"))
        assertEquals(9, settingsRepository.getSettings("user_B")?.hour)
    }

    // ============================================================
    // Edge Case Tests
    // ============================================================

    @Test
    fun `handleSignInSuccess - handles sign-in after previous sign-out`() = runTest {
        // Given: Previous sign-in/sign-out cycle, now new anonymous settings
        settingsRepository.setSettings(
            ANONYMOUS_USER_ID,
            NotificationSettings(enabled = true, hour = 7, minute = 15)
        )

        // When: User signs in again
        val result = useCase.handleSignInSuccess("firebase_user_456")

        // Then: Settings migrated successfully
        assertEquals(SignInNotificationResult.MigratedAnonymous, result)
        assertEquals(7, settingsRepository.getSettings("firebase_user_456")?.hour)
        assertEquals(15, settingsRepository.getSettings("firebase_user_456")?.minute)
    }
}
