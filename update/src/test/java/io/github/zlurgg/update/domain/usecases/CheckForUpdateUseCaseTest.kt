package io.github.zlurgg.update.domain.usecases

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import io.github.zlurgg.update.fake.FakeUpdatePreferencesRepository
import io.github.zlurgg.update.fake.FakeUpdateRepository
import io.github.zlurgg.update.domain.model.UpdateInfo

/**
 * Unit tests for CheckForUpdateUseCase.
 *
 * Tests version comparison logic and update checking behavior:
 * - Semantic version comparison (major, minor, patch)
 * - Handling of version prefixes (v1.0.0 vs 1.0.0)
 * - Dismissed version filtering
 * - Force check behavior
 * - Network error handling
 * - Missing APK handling
 */
class CheckForUpdateUseCaseTest {

    private lateinit var fakeUpdateRepository: FakeUpdateRepository
    private lateinit var fakePreferencesRepository: FakeUpdatePreferencesRepository
    private lateinit var checkForUpdateUseCase: CheckForUpdateUseCase

    private val currentVersion = "1.0.3"

    @Before
    fun setup() {
        fakeUpdateRepository = FakeUpdateRepository()
        fakePreferencesRepository = FakeUpdatePreferencesRepository()
        checkForUpdateUseCase = CheckForUpdateUseCase(
            updateRepository = fakeUpdateRepository,
            updatePreferencesRepository = fakePreferencesRepository,
            currentVersion = currentVersion
        )
    }

    // ============================================================
    // Version Comparison Tests
    // ============================================================

    @Test
    fun `isNewerVersion - 1_0_4 is newer than 1_0_3`() {
        assertTrue(checkForUpdateUseCase.isNewerVersion("1.0.4", "1.0.3"))
    }

    @Test
    fun `isNewerVersion - 1_1_0 is newer than 1_0_9`() {
        assertTrue(checkForUpdateUseCase.isNewerVersion("1.1.0", "1.0.9"))
    }

    @Test
    fun `isNewerVersion - 2_0_0 is newer than 1_9_9`() {
        assertTrue(checkForUpdateUseCase.isNewerVersion("2.0.0", "1.9.9"))
    }

    @Test
    fun `isNewerVersion - same version returns false`() {
        assertFalse(checkForUpdateUseCase.isNewerVersion("1.0.3", "1.0.3"))
    }

    @Test
    fun `isNewerVersion - older version returns false`() {
        assertFalse(checkForUpdateUseCase.isNewerVersion("1.0.2", "1.0.3"))
    }

    @Test
    fun `isNewerVersion - handles v prefix in remote version`() {
        assertTrue(checkForUpdateUseCase.isNewerVersion("v1.0.4", "1.0.3"))
    }

    @Test
    fun `isNewerVersion - handles v prefix in both versions`() {
        assertTrue(checkForUpdateUseCase.isNewerVersion("v1.0.4", "v1.0.3"))
    }

    @Test
    fun `isNewerVersion - handles different version lengths (remote longer)`() {
        assertTrue(checkForUpdateUseCase.isNewerVersion("1.0.0.1", "1.0.0"))
    }

    @Test
    fun `isNewerVersion - handles different version lengths (current longer)`() {
        // 1.0.0 is NOT newer than 1.0.0.1
        assertFalse(checkForUpdateUseCase.isNewerVersion("1.0.0", "1.0.0.1"))
    }

    @Test
    fun `isNewerVersion - handles two-part versions`() {
        assertTrue(checkForUpdateUseCase.isNewerVersion("1.1", "1.0"))
    }

    // ============================================================
    // Update Check Success Cases
    // ============================================================

    @Test
    fun `invoke - returns update info when newer version available`() = runTest {
        // Given: Newer version available
        val updateInfo = createTestUpdateInfo("1.0.4")
        fakeUpdateRepository.setLatestRelease(updateInfo)

        // When: Checking for updates
        val result = checkForUpdateUseCase()

        // Then: Should return update info
        assertNotNull("Should return update info", result)
        assertEquals("1.0.4", result?.versionName)
    }

    @Test
    fun `invoke - returns null when same version`() = runTest {
        // Given: Same version as current
        val updateInfo = createTestUpdateInfo("1.0.3")
        fakeUpdateRepository.setLatestRelease(updateInfo)

        // When: Checking for updates
        val result = checkForUpdateUseCase()

        // Then: Should return null
        assertNull("Should return null for same version", result)
    }

    @Test
    fun `invoke - returns null when older version`() = runTest {
        // Given: Older version than current
        val updateInfo = createTestUpdateInfo("1.0.2")
        fakeUpdateRepository.setLatestRelease(updateInfo)

        // When: Checking for updates
        val result = checkForUpdateUseCase()

        // Then: Should return null
        assertNull("Should return null for older version", result)
    }

    // ============================================================
    // Dismissed Version Tests
    // ============================================================

    @Test
    fun `invoke - dismissed version is skipped`() = runTest {
        // Given: Newer version available but dismissed
        val updateInfo = createTestUpdateInfo("1.0.4")
        fakeUpdateRepository.setLatestRelease(updateInfo)
        fakePreferencesRepository.setDismissedVersionForTest("1.0.4")

        // When: Checking for updates (not force)
        val result = checkForUpdateUseCase(forceCheck = false)

        // Then: Should return null
        assertNull("Should skip dismissed version", result)
    }

    @Test
    fun `invoke - dismissed version shown when force check`() = runTest {
        // Given: Newer version available but dismissed
        val updateInfo = createTestUpdateInfo("1.0.4")
        fakeUpdateRepository.setLatestRelease(updateInfo)
        fakePreferencesRepository.setDismissedVersionForTest("1.0.4")

        // When: Force checking for updates
        val result = checkForUpdateUseCase(forceCheck = true)

        // Then: Should return update info
        assertNotNull("Should show dismissed version on force check", result)
        assertEquals("1.0.4", result?.versionName)
    }

    @Test
    fun `invoke - different dismissed version does not affect update`() = runTest {
        // Given: Newer version available, different version dismissed
        val updateInfo = createTestUpdateInfo("1.0.4")
        fakeUpdateRepository.setLatestRelease(updateInfo)
        fakePreferencesRepository.setDismissedVersionForTest("1.0.3")

        // When: Checking for updates
        val result = checkForUpdateUseCase(forceCheck = false)

        // Then: Should return update info
        assertNotNull("Should show update for non-dismissed version", result)
        assertEquals("1.0.4", result?.versionName)
    }

    // ============================================================
    // Error Handling Tests
    // ============================================================

    @Test
    fun `invoke - network error returns null gracefully`() = runTest {
        // Given: Network error
        fakeUpdateRepository.setNetworkError()

        // When: Checking for updates
        val result = checkForUpdateUseCase()

        // Then: Should return null without crashing
        assertNull("Should return null on network error", result)
    }

    @Test
    fun `invoke - missing APK returns null`() = runTest {
        // Given: Newer version but no APK download URL
        val updateInfo = UpdateInfo(
            versionName = "1.0.4",
            releaseUrl = "https://github.com/test/releases/v1.0.4",
            apkDownloadUrl = null,
            apkSize = null,
            changelog = "New features"
        )
        fakeUpdateRepository.setLatestRelease(updateInfo)

        // When: Checking for updates
        val result = checkForUpdateUseCase()

        // Then: Should return null (can't download without APK)
        assertNull("Should return null when APK is missing", result)
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private fun createTestUpdateInfo(version: String): UpdateInfo {
        return UpdateInfo(
            versionName = version,
            releaseUrl = "https://github.com/test/releases/$version",
            apkDownloadUrl = "https://github.com/test/releases/download/$version/app.apk",
            apkSize = 10_000_000L,
            changelog = "Release notes for $version"
        )
    }
}
