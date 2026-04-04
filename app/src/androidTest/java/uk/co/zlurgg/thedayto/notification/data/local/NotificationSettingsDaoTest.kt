package uk.co.zlurgg.thedayto.notification.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.zlurgg.thedayto.base.DatabaseTest

/**
 * Instrumented tests for NotificationSettingsDao with real Room database.
 *
 * Tests:
 * - CRUD operations
 * - User isolation (each user has their own settings)
 * - Sync status queries
 */
@RunWith(AndroidJUnit4::class)
class NotificationSettingsDaoTest : DatabaseTest() {

    // ============================================================
    // Insert and Query Tests
    // ============================================================

    @Test
    fun upsert_inserts_new_settings() = runTest {
        // Given
        val settings = createEntity("user_123", enabled = true, hour = 10, minute = 30)

        // When
        notificationSettingsDao.upsert(settings)

        // Then
        val retrieved = notificationSettingsDao.getByUserId("user_123")
        assertNotNull(retrieved)
        assertEquals("user_123", retrieved!!.userId)
        assertEquals(true, retrieved.enabled)
        assertEquals(10, retrieved.hour)
        assertEquals(30, retrieved.minute)
    }

    @Test
    fun upsert_replaces_existing_settings() = runTest {
        // Given: Existing settings
        notificationSettingsDao.upsert(createEntity("user_123", enabled = true, hour = 9))

        // When: Upsert with same userId
        notificationSettingsDao.upsert(createEntity("user_123", enabled = false, hour = 20))

        // Then: Settings should be replaced
        val retrieved = notificationSettingsDao.getByUserId("user_123")
        assertEquals(false, retrieved!!.enabled)
        assertEquals(20, retrieved.hour)
    }

    @Test
    fun getByUserId_returns_null_for_nonexistent_user() = runTest {
        // When
        val result = notificationSettingsDao.getByUserId("nonexistent_user")

        // Then
        assertNull(result)
    }

    // ============================================================
    // User Isolation Tests
    // ============================================================

    @Test
    fun settings_are_isolated_per_user() = runTest {
        // Given: Different settings for different users
        notificationSettingsDao.upsert(createEntity("anonymous", enabled = true, hour = 8))
        notificationSettingsDao.upsert(createEntity("user_A", enabled = false, hour = 14))
        notificationSettingsDao.upsert(createEntity("user_B", enabled = true, hour = 20))

        // When/Then: Each user has their own settings
        val anonymous = notificationSettingsDao.getByUserId("anonymous")
        val userA = notificationSettingsDao.getByUserId("user_A")
        val userB = notificationSettingsDao.getByUserId("user_B")

        assertEquals(true, anonymous!!.enabled)
        assertEquals(8, anonymous.hour)

        assertEquals(false, userA!!.enabled)
        assertEquals(14, userA.hour)

        assertEquals(true, userB!!.enabled)
        assertEquals(20, userB.hour)
    }

    // ============================================================
    // Delete Tests
    // ============================================================

    @Test
    fun deleteByUserId_removes_settings() = runTest {
        // Given
        notificationSettingsDao.upsert(createEntity("user_to_delete"))

        // When
        notificationSettingsDao.deleteByUserId("user_to_delete")

        // Then
        assertNull(notificationSettingsDao.getByUserId("user_to_delete"))
    }

    @Test
    fun deleteByUserId_only_affects_specified_user() = runTest {
        // Given: Multiple users
        notificationSettingsDao.upsert(createEntity("user_A"))
        notificationSettingsDao.upsert(createEntity("user_B"))

        // When: Delete one user
        notificationSettingsDao.deleteByUserId("user_A")

        // Then: Other user unaffected
        assertNull(notificationSettingsDao.getByUserId("user_A"))
        assertNotNull(notificationSettingsDao.getByUserId("user_B"))
    }

    // ============================================================
    // Sync Status Tests
    // ============================================================

    @Test
    fun updateSyncStatus_updates_only_sync_status() = runTest {
        // Given
        notificationSettingsDao.upsert(createEntity("user_123", syncStatus = "PENDING_SYNC"))

        // When
        notificationSettingsDao.updateSyncStatus("user_123", "SYNCED")

        // Then
        val retrieved = notificationSettingsDao.getByUserId("user_123")
        assertEquals("SYNCED", retrieved!!.syncStatus)
    }

    @Test
    fun getPendingSync_returns_settings_with_pending_status() = runTest {
        // Given
        notificationSettingsDao.upsert(createEntity("user_123", syncStatus = "PENDING_SYNC"))

        // When
        val pending = notificationSettingsDao.getPendingSync("user_123")

        // Then
        assertNotNull(pending)
        assertEquals("PENDING_SYNC", pending!!.syncStatus)
    }

    @Test
    fun getPendingSync_returns_null_for_synced_settings() = runTest {
        // Given
        notificationSettingsDao.upsert(createEntity("user_123", syncStatus = "SYNCED"))

        // When
        val pending = notificationSettingsDao.getPendingSync("user_123")

        // Then
        assertNull(pending)
    }

    @Test
    fun getPendingSync_returns_null_for_local_only_settings() = runTest {
        // Given
        notificationSettingsDao.upsert(createEntity("user_123", syncStatus = "LOCAL_ONLY"))

        // When
        val pending = notificationSettingsDao.getPendingSync("user_123")

        // Then
        assertNull(pending)
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private fun createEntity(
        userId: String,
        enabled: Boolean = true,
        hour: Int = 9,
        minute: Int = 0,
        syncStatus: String = "LOCAL_ONLY"
    ) = NotificationSettingsEntity(
        userId = userId,
        enabled = enabled,
        hour = hour,
        minute = minute,
        syncId = "sync-$userId",
        syncStatus = syncStatus,
        updatedAt = System.currentTimeMillis()
    )
}
