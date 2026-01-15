package io.github.zlurgg.update.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.zlurgg.update.domain.model.UpdateInfo
import io.github.zlurgg.update.ui.strings.UpToDateDialogStrings

private val paddingSmall = 8.dp
private val paddingMedium = 16.dp

/**
 * Dialog shown when user manually checks for updates and is already on the latest version.
 * Displays current version and release notes (fetched from GitHub).
 *
 * @param currentVersionInfo Optional info about the current version (for release notes)
 * @param currentVersionName The current version string to display
 * @param onDismiss Callback when user dismisses the dialog
 * @param strings Customizable string resources for localization
 * @param modifier Optional modifier
 */
@Composable
fun UpToDateDialog(
    currentVersionInfo: UpdateInfo?,
    currentVersionName: String,
    onDismiss: () -> Unit,
    strings: UpToDateDialogStrings = UpToDateDialogStrings(),
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Version section
                Text(
                    text = strings.currentVersionLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(paddingSmall))
                Text(
                    text = currentVersionInfo?.versionName ?: currentVersionName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Release notes section (if available)
                currentVersionInfo?.changelog?.let { changelog ->
                    if (changelog.isNotBlank()) {
                        Spacer(modifier = Modifier.height(paddingMedium))

                        Text(
                            text = strings.inThisVersionLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(paddingSmall))

                        // Reuse the ChangelogContent from UpdateDialog
                        ChangelogContent(changelog = changelog)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.okButton)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun UpToDateDialogPreview() {
    MaterialTheme {
        UpToDateDialog(
            currentVersionInfo = UpdateInfo(
                versionName = "1.0.5",
                releaseUrl = "https://github.com/example/releases/v1.0.5",
                apkDownloadUrl = null,
                apkSize = null,
                changelog = """
                    ## Features
                    - In-app update checker
                    - Edit mood color names

                    ## Improvements
                    - Better accessibility support
                    - Performance improvements
                """.trimIndent()
            ),
            currentVersionName = "1.0.5",
            onDismiss = {}
        )
    }
}

@Preview(name = "No Changelog", showBackground = true)
@Composable
private fun UpToDateDialogNoChangelogPreview() {
    MaterialTheme {
        UpToDateDialog(
            currentVersionInfo = null,
            currentVersionName = "1.0.5",
            onDismiss = {}
        )
    }
}
