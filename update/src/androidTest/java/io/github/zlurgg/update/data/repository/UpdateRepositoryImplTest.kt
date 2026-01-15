package io.github.zlurgg.update.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.Result
import io.github.zlurgg.update.data.remote.api.GitHubApiService
import io.github.zlurgg.update.data.remote.dto.AssetDto
import io.github.zlurgg.update.data.remote.dto.GitHubReleaseDto
import io.github.zlurgg.update.data.service.ApkDownloadService
import io.github.zlurgg.update.domain.model.UpdateConfig
import java.io.IOException

/**
 * Integration tests for UpdateRepositoryImpl using MockK.
 *
 * Tests cover:
 * - DTO to domain model mapping
 * - Error handling for API failures
 * - Handling releases without APK assets
 * - Delegation to ApkDownloadService
 */
@RunWith(AndroidJUnit4::class)
class UpdateRepositoryImplTest {

    private val mockGitHubApi = mockk<GitHubApiService>()
    private val mockApkService = mockk<ApkDownloadService>(relaxed = true)
    private val config = UpdateConfig(
        gitHubOwner = "Zlurgg",
        gitHubRepo = "The-Day-To",
        appName = "the-day-to"
    )
    private lateinit var repository: UpdateRepositoryImpl

    @Before
    fun setup() {
        repository = UpdateRepositoryImpl(mockGitHubApi, mockApkService, config)
    }

    // ============================================================
    // getLatestRelease Tests
    // ============================================================

    @Test
    fun getLatestRelease_maps_DTO_correctly() = runTest {
        // Given: A valid GitHub release response
        val releaseDto = GitHubReleaseDto(
            tagName = "v1.0.5",
            name = "Release 1.0.5",
            htmlUrl = "https://github.com/Zlurgg/The-Day-To/releases/tag/v1.0.5",
            body = "- Bug fixes\n- New features",
            assets = listOf(
                AssetDto(
                    name = "app-release.apk",
                    downloadUrl = "https://github.com/Zlurgg/The-Day-To/releases/download/v1.0.5/app-release.apk",
                    size = 3_500_000L
                )
            )
        )

        coEvery {
            mockGitHubApi.getLatestRelease(any(), any())
        } returns releaseDto

        // When: Fetching latest release
        val result = repository.getLatestRelease()

        // Then: Should return success with correctly mapped domain model
        assertTrue("Result should be success", result is Result.Success)
        val updateInfo = (result as Result.Success).data

        assertEquals("1.0.5", updateInfo.versionName) // 'v' prefix removed
        assertEquals("https://github.com/Zlurgg/The-Day-To/releases/tag/v1.0.5", updateInfo.releaseUrl)
        assertEquals("https://github.com/Zlurgg/The-Day-To/releases/download/v1.0.5/app-release.apk", updateInfo.apkDownloadUrl)
        assertEquals(3_500_000L, updateInfo.apkSize)
        assertEquals("- Bug fixes\n- New features", updateInfo.changelog)
    }

    @Test
    fun getLatestRelease_returns_failure_on_exception() = runTest {
        // Given: API throws an exception
        coEvery {
            mockGitHubApi.getLatestRelease(any(), any())
        } throws IOException("Network error")

        // When: Fetching latest release
        val result = repository.getLatestRelease()

        // Then: Should return error
        assertTrue("Result should be error", result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(
            "Error should be a Remote error",
            error is DataError.Remote
        )
    }

    @Test
    fun getLatestRelease_handles_missing_APK_asset() = runTest {
        // Given: A release without APK asset
        val releaseDto = GitHubReleaseDto(
            tagName = "v1.0.5",
            name = "Release 1.0.5",
            htmlUrl = "https://github.com/Zlurgg/The-Day-To/releases/tag/v1.0.5",
            body = "Source code only release",
            assets = emptyList() // No APK
        )

        coEvery {
            mockGitHubApi.getLatestRelease(any(), any())
        } returns releaseDto

        // When: Fetching latest release
        val result = repository.getLatestRelease()

        // Then: Should return success but with null APK fields
        assertTrue("Result should be success", result is Result.Success)
        val updateInfo = (result as Result.Success).data

        assertEquals("1.0.5", updateInfo.versionName)
        assertNull("APK download URL should be null", updateInfo.apkDownloadUrl)
        assertNull("APK size should be null", updateInfo.apkSize)
    }

    // ============================================================
    // Delegation Tests
    // ============================================================

    @Test
    fun downloadApk_delegates_to_service() {
        // Given: Service returns a download ID
        every {
            mockApkService.downloadApk(any(), any())
        } returns 12345L

        // When: Calling downloadApk
        val result = repository.downloadApk(
            url = "https://example.com/app.apk",
            fileName = "app-release.apk"
        )

        // Then: Should delegate to service and return the ID
        assertEquals(12345L, result)
        verify {
            mockApkService.downloadApk(
                "https://example.com/app.apk",
                "app-release.apk"
            )
        }
    }

    @Test
    fun installApk_delegates_to_service() {
        // When: Calling installApk
        repository.installApk(12345L)

        // Then: Should delegate to service
        verify {
            mockApkService.installApk(12345L)
        }
    }
}
