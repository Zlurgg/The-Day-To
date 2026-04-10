package uk.co.zlurgg.thedayto.notification.data.migration

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.fake.FakeAuthRepository
import uk.co.zlurgg.thedayto.fake.FakeNotificationSettingsDao
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID

/**
 * Unit tests for NotificationMigrationService.
 *
 * Tests the migration from SharedPreferences to Room for notification settings.
 */
class NotificationMigrationServiceTest {

    private lateinit var fakeDao: FakeNotificationSettingsDao
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var migrationService: NotificationMigrationService

    @Before
    fun setup() {
        fakeDao = FakeNotificationSettingsDao()
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)
        fakeAuthRepository = FakeAuthRepository()

        // Configure editor to return itself for chaining
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor

        migrationService = NotificationMigrationService(
            dao = fakeDao,
            legacyPrefs = mockPrefs,
            authRepository = fakeAuthRepository,
        )
    }

    // ============================================================
    // No Migration Needed Tests
    // ============================================================

    @Test
    fun `migrateIfNeeded does nothing when no legacy data exists`() = runTest {
        // Given: No legacy SharedPreferences keys
        every { mockPrefs.contains("notification_enabled") } returns false

        // When: Migration is attempted
        migrationService.migrateIfNeeded()

        // Then: No data should be in Room
        assertNull(fakeDao.getByUserId(ANONYMOUS_USER_ID))
    }

    @Test
    fun `migrateIfNeeded only cleans up prefs when Room already has data`() = runTest {
        // Given: Legacy data exists and Room already has data
        every { mockPrefs.contains("notification_enabled") } returns true
        fakeDao.upsert(createTestEntity(ANONYMOUS_USER_ID))

        // When: Migration is attempted
        migrationService.migrateIfNeeded()

        // Then: SharedPreferences should be cleaned up
        verify { mockEditor.remove("notification_enabled") }
        verify { mockEditor.remove("notification_hour") }
        verify { mockEditor.remove("notification_minute") }
    }

    // ============================================================
    // Anonymous User Migration Tests
    // ============================================================

    @Test
    fun `migrateIfNeeded migrates enabled notifications for anonymous user`() = runTest {
        // Given: Legacy settings for anonymous user
        every { mockPrefs.contains("notification_enabled") } returns true
        every { mockPrefs.getBoolean("notification_enabled", false) } returns true
        every { mockPrefs.getInt("notification_hour", 9) } returns 10
        every { mockPrefs.getInt("notification_minute", 0) } returns 30

        // When: Migration runs
        migrationService.migrateIfNeeded()

        // Then: Data should be in Room
        val settings = fakeDao.getByUserId(ANONYMOUS_USER_ID)
        assertNotNull(settings)
        assertEquals(true, settings!!.enabled)
        assertEquals(10, settings.hour)
        assertEquals(30, settings.minute)
        assertEquals("PENDING_SYNC", settings.syncStatus)
    }

    @Test
    fun `migrateIfNeeded migrates disabled notifications for anonymous user`() = runTest {
        // Given: Disabled notifications
        every { mockPrefs.contains("notification_enabled") } returns true
        every { mockPrefs.getBoolean("notification_enabled", false) } returns false
        every { mockPrefs.getInt("notification_hour", 9) } returns 9
        every { mockPrefs.getInt("notification_minute", 0) } returns 0

        // When: Migration runs
        migrationService.migrateIfNeeded()

        // Then: Data should be in Room with enabled = false
        val settings = fakeDao.getByUserId(ANONYMOUS_USER_ID)
        assertNotNull(settings)
        assertEquals(false, settings!!.enabled)
    }

    // ============================================================
    // Signed-In User Migration Tests
    // ============================================================

    @Test
    fun `migrateIfNeeded migrates settings to signed-in user userId`() = runTest {
        // Given: A signed-in user and legacy settings
        fakeAuthRepository.setSignedInUser(
            UserData(
                userId = "firebase_user_123",
                username = "Test User",
                profilePictureUrl = null,
            ),
        )
        every { mockPrefs.contains("notification_enabled") } returns true
        every { mockPrefs.getBoolean("notification_enabled", false) } returns true
        every { mockPrefs.getInt("notification_hour", 9) } returns 8
        every { mockPrefs.getInt("notification_minute", 0) } returns 0

        // When: Migration runs
        migrationService.migrateIfNeeded()

        // Then: Data should be stored under the Firebase UID
        val settings = fakeDao.getByUserId("firebase_user_123")
        assertNotNull(settings)
        assertEquals("firebase_user_123", settings!!.userId)
        assertEquals(true, settings.enabled)
        assertEquals(8, settings.hour)
    }

    // ============================================================
    // Idempotency Tests
    // ============================================================

    @Test
    fun `migrateIfNeeded is idempotent - multiple calls don't duplicate data`() = runTest {
        // Given: Legacy settings
        every { mockPrefs.contains("notification_enabled") } returns true
        every { mockPrefs.getBoolean("notification_enabled", false) } returns true
        every { mockPrefs.getInt("notification_hour", 9) } returns 14
        every { mockPrefs.getInt("notification_minute", 0) } returns 45

        // When: Migration runs twice
        migrationService.migrateIfNeeded()

        // Second call - now Room has data, SharedPreferences have been "removed"
        // Simulate that contains() returns false after cleanup
        every { mockPrefs.contains("notification_enabled") } returns false
        migrationService.migrateIfNeeded()

        // Then: Only one entry in Room
        assertEquals(1, fakeDao.getAllSettings().size)
    }

    // ============================================================
    // Cleanup Tests
    // ============================================================

    @Test
    fun `migrateIfNeeded cleans up SharedPreferences after migration`() = runTest {
        // Given: Legacy settings
        every { mockPrefs.contains("notification_enabled") } returns true
        every { mockPrefs.getBoolean("notification_enabled", false) } returns true
        every { mockPrefs.getInt("notification_hour", 9) } returns 9
        every { mockPrefs.getInt("notification_minute", 0) } returns 0

        // When: Migration runs
        migrationService.migrateIfNeeded()

        // Then: All legacy keys should be removed
        verify { mockEditor.remove("notification_enabled") }
        verify { mockEditor.remove("notification_hour") }
        verify { mockEditor.remove("notification_minute") }
        verify { mockEditor.apply() }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private fun createTestEntity(userId: String) =
        uk.co.zlurgg.thedayto.notification.data.local.NotificationSettingsEntity(
            userId = userId,
            enabled = true,
            hour = 9,
            minute = 0,
            syncId = "test-sync-id",
            syncStatus = "SYNCED",
            updatedAt = System.currentTimeMillis(),
        )
}
