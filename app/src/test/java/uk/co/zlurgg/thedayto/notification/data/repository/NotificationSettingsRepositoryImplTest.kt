package uk.co.zlurgg.thedayto.notification.data.repository

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.fake.FakeNotificationSettingsDao
import uk.co.zlurgg.thedayto.notification.data.local.NotificationSettingsEntity
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettingsState

/**
 * Unit tests for NotificationSettingsRepositoryImpl.
 */
class NotificationSettingsRepositoryImplTest {

    private lateinit var fakeDao: FakeNotificationSettingsDao
    private lateinit var mockMigrationService: NotificationMigrationService
    private lateinit var repository: NotificationSettingsRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeNotificationSettingsDao()
        mockMigrationService = mockk(relaxed = true)
        repository = NotificationSettingsRepositoryImpl(
            dao = fakeDao,
            migrationService = mockMigrationService,
        )
    }

    // ============================================================
    // Migration Tests
    // ============================================================

    @Test
    fun `first access triggers migration`() = runTest {
        // When: First access to repository
        repository.getSettings("test_user")

        // Then: Migration should be called
        coVerify(exactly = 1) { mockMigrationService.migrateIfNeeded() }
    }

    @Test
    fun `subsequent accesses do not trigger migration`() = runTest {
        // When: Multiple accesses
        repository.getSettings("test_user")
        repository.getSettings("test_user")
        repository.getSettings("test_user")

        // Then: Migration should only be called once
        coVerify(exactly = 1) { mockMigrationService.migrateIfNeeded() }
    }

    // ============================================================
    // getSettingsState Tests
    // ============================================================

    @Test
    fun `getSettingsState returns NotConfigured when no settings exist`() = runTest {
        // Given: No settings in database

        // When
        val result = repository.getSettingsState("user_123")

        // Then
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data is NotificationSettingsState.NotConfigured)
    }

    @Test
    fun `getSettingsState returns Configured with settings when they exist`() = runTest {
        // Given: Settings exist
        fakeDao.upsert(createValidEntity("user_123", enabled = true, hour = 10, minute = 30))

        // When
        val result = repository.getSettingsState("user_123")

        // Then
        assertTrue(result is Result.Success)
        val state = (result as Result.Success).data
        assertTrue(state is NotificationSettingsState.Configured)
        val configured = state as NotificationSettingsState.Configured
        assertEquals(true, configured.settings.enabled)
        assertEquals(10, configured.settings.hour)
        assertEquals(30, configured.settings.minute)
    }

    @Test
    fun `getSettingsState returns NotConfigured for corrupt data`() = runTest {
        // Given: Corrupt settings (invalid hour)
        fakeDao.upsert(createCorruptEntity("user_123"))

        // When
        val result = repository.getSettingsState("user_123")

        // Then: Should treat as not configured
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data is NotificationSettingsState.NotConfigured)
    }

    // ============================================================
    // getSettings Tests
    // ============================================================

    @Test
    fun `getSettings returns null when no settings exist`() = runTest {
        // When
        val result = repository.getSettings("user_123")

        // Then
        assertTrue(result is Result.Success)
        assertNull((result as Result.Success).data)
    }

    @Test
    fun `getSettings returns domain model when settings exist`() = runTest {
        // Given
        fakeDao.upsert(createValidEntity("user_123", enabled = false, hour = 14, minute = 0))

        // When
        val result = repository.getSettings("user_123")

        // Then
        assertTrue(result is Result.Success)
        val settings = (result as Result.Success).data
        assertNotNull(settings)
        assertEquals(false, settings!!.enabled)
        assertEquals(14, settings.hour)
        assertEquals(0, settings.minute)
    }

    @Test
    fun `getSettings returns null for corrupt data`() = runTest {
        // Given: Invalid minute
        fakeDao.upsert(
            NotificationSettingsEntity(
                userId = "user_123",
                enabled = true,
                hour = 9,
                minute = 99, // Invalid
                syncId = "test-id",
                syncStatus = "SYNCED",
                updatedAt = 0,
            ),
        )

        // When
        val result = repository.getSettings("user_123")

        // Then
        assertTrue(result is Result.Success)
        assertNull((result as Result.Success).data)
    }

    // ============================================================
    // saveSettings Tests
    // ============================================================

    @Test
    fun `saveSettings creates new entity for new user`() = runTest {
        // Given
        val settings = NotificationSettings(enabled = true, hour = 8, minute = 15)

        // When
        repository.saveSettings("new_user", settings)

        // Then
        val saved = fakeDao.getByUserId("new_user")
        assertNotNull(saved)
        assertEquals("new_user", saved!!.userId)
        assertEquals(true, saved.enabled)
        assertEquals(8, saved.hour)
        assertEquals(15, saved.minute)
        assertEquals("PENDING_SYNC", saved.syncStatus)
    }

    @Test
    fun `saveSettings preserves syncId for existing user`() = runTest {
        // Given: Existing settings with specific syncId
        val existingSyncId = "original-sync-id"
        fakeDao.upsert(createValidEntity("user_123", syncId = existingSyncId))

        // When: Save new settings
        repository.saveSettings("user_123", NotificationSettings(enabled = false, hour = 20, minute = 0))

        // Then: syncId should be preserved
        val saved = fakeDao.getByUserId("user_123")
        assertNotNull(saved)
        assertEquals(existingSyncId, saved!!.syncId)
        assertEquals(false, saved.enabled)
        assertEquals(20, saved.hour)
    }

    @Test
    fun `saveSettings marks as PENDING_SYNC for existing user`() = runTest {
        // Given: Existing synced settings
        fakeDao.upsert(createValidEntity("user_123", syncStatus = "SYNCED"))

        // When: Save new settings
        repository.saveSettings("user_123", NotificationSettings(enabled = true, hour = 7, minute = 30))

        // Then: Should be marked as pending sync
        val saved = fakeDao.getByUserId("user_123")
        assertEquals("PENDING_SYNC", saved!!.syncStatus)
    }

    // ============================================================
    // deleteSettings Tests
    // ============================================================

    @Test
    fun `deleteSettings removes settings for user`() = runTest {
        // Given
        fakeDao.upsert(createValidEntity("user_to_delete"))

        // When
        repository.deleteSettings("user_to_delete")

        // Then
        assertNull(fakeDao.getByUserId("user_to_delete"))
    }

    @Test
    fun `deleteSettings is safe when no settings exist`() = runTest {
        // When: Delete non-existent user
        repository.deleteSettings("non_existent_user")

        // Then: No exception should be thrown
        assertNull(fakeDao.getByUserId("non_existent_user"))
    }

    // ============================================================
    // Multi-user Tests
    // ============================================================

    @Test
    fun `repository correctly isolates settings per user`() = runTest {
        // Given: Settings for multiple users
        fakeDao.upsert(createValidEntity("user_A", enabled = true, hour = 8))
        fakeDao.upsert(createValidEntity("user_B", enabled = false, hour = 20))

        // When
        val resultA = repository.getSettings("user_A")
        val resultB = repository.getSettings("user_B")

        // Then
        assertTrue(resultA is Result.Success)
        assertTrue(resultB is Result.Success)
        val settingsA = (resultA as Result.Success).data!!
        val settingsB = (resultB as Result.Success).data!!
        assertEquals(true, settingsA.enabled)
        assertEquals(8, settingsA.hour)
        assertEquals(false, settingsB.enabled)
        assertEquals(20, settingsB.hour)
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private fun createValidEntity(
        userId: String,
        enabled: Boolean = true,
        hour: Int = 9,
        minute: Int = 0,
        syncId: String = "test-sync-id",
        syncStatus: String = "SYNCED",
    ) = NotificationSettingsEntity(
        userId = userId,
        enabled = enabled,
        hour = hour,
        minute = minute,
        syncId = syncId,
        syncStatus = syncStatus,
        updatedAt = System.currentTimeMillis(),
    )

    private fun createCorruptEntity(userId: String) = NotificationSettingsEntity(
        userId = userId,
        enabled = true,
        hour = 25, // Invalid - should be 0-23
        minute = 0,
        syncId = "test-sync-id",
        syncStatus = "SYNCED",
        updatedAt = System.currentTimeMillis(),
    )
}
