package uk.co.zlurgg.thedayto.update.domain.usecases

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakeUpdateRepository
import uk.co.zlurgg.thedayto.update.domain.model.UpdateConfig
import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo

/**
 * Unit tests for DownloadUpdateUseCase.
 *
 * Tests APK download initiation:
 * - Downloads APK when URL is available
 * - Returns null when APK URL is missing
 * - Generates correct filename
 */
class DownloadUpdateUseCaseTest {

    private lateinit var fakeUpdateRepository: FakeUpdateRepository
    private lateinit var downloadUpdateUseCase: DownloadUpdateUseCase
    private lateinit var testConfig: UpdateConfig

    @Before
    fun setup() {
        fakeUpdateRepository = FakeUpdateRepository()
        testConfig = UpdateConfig(
            gitHubOwner = "TestOwner",
            gitHubRepo = "TestRepo",
            appName = "test-app"
        )
        downloadUpdateUseCase = DownloadUpdateUseCase(
            updateRepository = fakeUpdateRepository,
            config = testConfig
        )
    }

    @Test
    fun `invoke - returns download ID when APK URL is available`() {
        // Given: Update info with APK URL
        val updateInfo = createTestUpdateInfo("1.0.4")

        // When: Downloading update
        val downloadId = downloadUpdateUseCase(updateInfo)

        // Then: Should return download ID
        assertNotNull("Should return download ID", downloadId)
        assertEquals(1L, downloadId)
    }

    @Test
    fun `invoke - returns null when APK URL is missing`() {
        // Given: Update info without APK URL
        val updateInfo = UpdateInfo(
            versionName = "1.0.4",
            releaseUrl = "https://github.com/test/releases/v1.0.4",
            apkDownloadUrl = null,
            apkSize = null,
            changelog = "New features"
        )

        // When: Downloading update
        val downloadId = downloadUpdateUseCase(updateInfo)

        // Then: Should return null
        assertNull("Should return null when APK URL is missing", downloadId)
    }

    @Test
    fun `invoke - downloads APK with correct URL and filename`() {
        // Given: Update info with specific version
        val updateInfo = createTestUpdateInfo("1.0.4")

        // When: Downloading update
        downloadUpdateUseCase(updateInfo)

        // Then: Should have called download with correct parameters
        val downloads = fakeUpdateRepository.getDownloadedApks()
        assertEquals("Should have 1 download", 1, downloads.size)
        assertEquals(
            "URL should match",
            "https://github.com/test/releases/download/1.0.4/app.apk",
            downloads[0].first
        )
        assertEquals(
            "Filename should be formatted correctly",
            "test-app-1.0.4.apk",
            downloads[0].second
        )
    }

    @Test
    fun `invoke - generates correct filename with v prefix in version`() {
        // Given: Update info with v prefix in version
        val updateInfo = UpdateInfo(
            versionName = "v1.0.4",
            releaseUrl = "https://github.com/test/releases/v1.0.4",
            apkDownloadUrl = "https://example.com/app.apk",
            apkSize = 10_000_000L,
            changelog = null
        )

        // When: Downloading update
        downloadUpdateUseCase(updateInfo)

        // Then: Filename should include v prefix as-is
        val downloads = fakeUpdateRepository.getDownloadedApks()
        assertEquals("test-app-v1.0.4.apk", downloads[0].second)
    }

    @Test
    fun `invoke - multiple downloads get sequential IDs`() {
        // Given: Multiple update infos
        val updateInfo1 = createTestUpdateInfo("1.0.4")
        val updateInfo2 = createTestUpdateInfo("1.0.5")

        // When: Downloading multiple updates
        val downloadId1 = downloadUpdateUseCase(updateInfo1)
        val downloadId2 = downloadUpdateUseCase(updateInfo2)

        // Then: IDs should be sequential
        assertEquals(1L, downloadId1)
        assertEquals(2L, downloadId2)
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
