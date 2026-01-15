package io.github.zlurgg.update.data.service

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for ApkDownloadService using MockK.
 *
 * Tests cover:
 * - Install APK intent creation
 * - Handling null URI for failed downloads
 *
 * Note: Download tests require real Android context for DownloadManager.Request
 * to properly set destination directories, so those are tested via manual/E2E testing.
 */
@RunWith(AndroidJUnit4::class)
class ApkDownloadServiceTest {

    private val mockContext = mockk<Context>(relaxed = true)
    private val mockDownloadManager = mockk<DownloadManager>(relaxed = true)
    private lateinit var service: ApkDownloadService

    @Before
    fun setup() {
        // Mock context to return our mock DownloadManager
        every {
            mockContext.getSystemService(Context.DOWNLOAD_SERVICE)
        } returns mockDownloadManager

        service = ApkDownloadService(mockContext)
    }

    // ============================================================
    // installApk Tests
    // ============================================================

    @Test
    fun installApk_starts_activity_when_uri_available() {
        // Given: DownloadManager returns a valid URI
        val mockUri = mockk<Uri>()
        every {
            mockDownloadManager.getUriForDownloadedFile(12345L)
        } returns mockUri

        // When: Installing APK
        service.installApk(12345L)

        // Then: Should start activity with intent
        verify {
            mockContext.startActivity(any())
        }
    }

    @Test
    fun installApk_handles_null_uri_gracefully() {
        // Given: DownloadManager returns null URI (download failed)
        every {
            mockDownloadManager.getUriForDownloadedFile(12345L)
        } returns null

        // When: Installing APK - should not crash
        service.installApk(12345L)

        // Then: Should NOT start activity (no URI to install)
        verify(exactly = 0) {
            mockContext.startActivity(any())
        }
    }
}
