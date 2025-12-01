package uk.co.zlurgg.thedayto.update.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo

/**
 * Dialog shown when user manually checks for updates and is already on the latest version.
 * Displays current version and release notes (fetched from GitHub).
 */
@Composable
fun UpToDateDialog(
    currentVersionInfo: UpdateInfo?,
    currentVersionName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.update_up_to_date_title),
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
                    text = stringResource(R.string.update_current_version_label),
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
                            text = stringResource(R.string.update_in_this_version_label),
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
                Text(stringResource(R.string.update_ok_button))
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
    TheDayToTheme {
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
    TheDayToTheme {
        UpToDateDialog(
            currentVersionInfo = null,
            currentVersionName = "1.0.5",
            onDismiss = {}
        )
    }
}
