package uk.co.zlurgg.thedayto.update.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.zlurgg.thedayto.base.ComposeTest
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo

/**
 * UI tests for UpdateDialog.
 *
 * Tests cover:
 * - Dialog displays version number
 * - Dialog shows changelog content
 * - Download button triggers callback
 * - Dismiss button triggers callback
 * - File size is formatted correctly (KB/MB)
 */
@RunWith(AndroidJUnit4::class)
class UpdateDialogTest : ComposeTest() {

    private val testUpdateInfo = UpdateInfo(
        versionName = "1.0.5",
        releaseUrl = "https://github.com/example/releases/v1.0.5",
        apkDownloadUrl = "https://example.com/app.apk",
        apkSize = 3_500_000L, // 3.5 MB
        changelog = "- Bug fixes\n- Performance improvements"
    )

    // ============================================================
    // Display Tests
    // ============================================================

    @Test
    fun dialog_displays_version_number() {
        composeTestRule.setContent {
            TheDayToTheme {
                UpdateDialog(
                    updateInfo = testUpdateInfo,
                    onDownload = {},
                    onDismiss = {}
                )
            }
        }

        // Verify dialog title
        composeTestRule
            .onNodeWithText("Update Available")
            .assertIsDisplayed()

        // Verify version is displayed
        composeTestRule
            .onNodeWithText("Version 1.0.5")
            .assertIsDisplayed()
    }

    @Test
    fun dialog_shows_changelog_content() {
        composeTestRule.setContent {
            TheDayToTheme {
                UpdateDialog(
                    updateInfo = testUpdateInfo,
                    onDownload = {},
                    onDismiss = {}
                )
            }
        }

        // Verify "What's new" section header
        composeTestRule
            .onNodeWithText("What's new:")
            .assertIsDisplayed()

        // Verify changelog content is displayed
        composeTestRule
            .onNodeWithText("- Bug fixes\n- Performance improvements")
            .assertIsDisplayed()
    }

    @Test
    fun file_size_formatted_correctly_as_mb() {
        composeTestRule.setContent {
            TheDayToTheme {
                UpdateDialog(
                    updateInfo = testUpdateInfo,
                    onDownload = {},
                    onDismiss = {}
                )
            }
        }

        // 3,500,000 bytes = 3.4 MB (approximately)
        composeTestRule
            .onNodeWithText("3.3 MB", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun file_size_formatted_correctly_as_kb() {
        val smallUpdateInfo = testUpdateInfo.copy(apkSize = 500_000L) // 500 KB

        composeTestRule.setContent {
            TheDayToTheme {
                UpdateDialog(
                    updateInfo = smallUpdateInfo,
                    onDownload = {},
                    onDismiss = {}
                )
            }
        }

        // 500,000 bytes = 488 KB
        composeTestRule
            .onNodeWithText("488 KB")
            .assertIsDisplayed()
    }

    // ============================================================
    // Callback Tests
    // ============================================================

    @Test
    fun download_button_triggers_callback() {
        var downloadClicked = false

        composeTestRule.setContent {
            TheDayToTheme {
                UpdateDialog(
                    updateInfo = testUpdateInfo,
                    onDownload = { downloadClicked = true },
                    onDismiss = {}
                )
            }
        }

        // Click download button
        composeTestRule
            .onNodeWithText("Download")
            .performClick()

        // Verify callback was triggered
        assertTrue("onDownload should be called", downloadClicked)
    }

    @Test
    fun dismiss_button_triggers_callback() {
        var dismissClicked = false

        composeTestRule.setContent {
            TheDayToTheme {
                UpdateDialog(
                    updateInfo = testUpdateInfo,
                    onDownload = {},
                    onDismiss = { dismissClicked = true }
                )
            }
        }

        // Click "Not Now" button
        composeTestRule
            .onNodeWithText("Not Now")
            .performClick()

        // Verify callback was triggered
        assertTrue("onDismiss should be called", dismissClicked)
    }
}
