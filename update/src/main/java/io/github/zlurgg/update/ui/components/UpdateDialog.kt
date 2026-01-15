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
import io.github.zlurgg.update.ui.strings.UpdateDialogStrings
import java.util.Locale

private val paddingSmall = 8.dp
private val paddingMedium = 16.dp

/**
 * Dialog to inform user about an available app update.
 * Follows Material 3 design guidelines and app dialog patterns.
 *
 * @param updateInfo Information about the available update
 * @param onDownload Callback when user clicks download
 * @param onDismiss Callback when user dismisses the dialog
 * @param strings Customizable string resources for localization
 * @param modifier Optional modifier
 */
@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
    strings: UpdateDialogStrings = UpdateDialogStrings(),
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
                    text = strings.versionLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(paddingSmall))
                Text(
                    text = updateInfo.versionName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // File size (if available)
                updateInfo.apkSize?.let { size ->
                    Text(
                        text = formatFileSize(size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // What's new section
                updateInfo.changelog?.let { changelog ->
                    if (changelog.isNotBlank()) {
                        Spacer(modifier = Modifier.height(paddingMedium))

                        Text(
                            text = strings.whatsNewLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(paddingSmall))

                        // Parse and display changelog with proper formatting
                        ChangelogContent(changelog = changelog)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDownload) {
                Text(strings.downloadButton)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.notNowButton)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    )
}

/**
 * Displays changelog content with proper formatting.
 * Parses markdown-style bullet points (lines starting with - or *) into a formatted list.
 * Shared by UpdateDialog and UpToDateDialog.
 */
@Composable
internal fun ChangelogContent(changelog: String) {
    val lines = changelog.lines().filter { it.isNotBlank() }

    Column {
        lines.forEach { line ->
            val trimmedLine = line.trim()
            val isBulletPoint = trimmedLine.startsWith("-") || trimmedLine.startsWith("*")

            if (isBulletPoint) {
                // Remove bullet and display with bullet character
                val content = trimmedLine.removePrefix("-").removePrefix("*").trim()
                Text(
                    text = "• $content",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else if (trimmedLine.startsWith("##")) {
                // Handle markdown headers (e.g., "## Features")
                val headerText = trimmedLine.removePrefix("##").trim()
                Spacer(modifier = Modifier.height(paddingSmall))
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                // Regular text
                Text(
                    text = trimmedLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun UpdateDialogPreview() {
    MaterialTheme {
        UpdateDialog(
            updateInfo = UpdateInfo(
                versionName = "1.0.5",
                releaseUrl = "https://github.com/example/releases/v1.0.5",
                apkDownloadUrl = "https://example.com/app.apk",
                apkSize = 3_500_000,
                changelog = """
                    ## Features
                    - In-app update checker
                    - Edit mood color names

                    ## Improvements
                    - Better accessibility support
                    - Performance improvements
                """.trimIndent()
            ),
            onDownload = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "No Changelog", showBackground = true)
@Composable
private fun UpdateDialogNoChangelogPreview() {
    MaterialTheme {
        UpdateDialog(
            updateInfo = UpdateInfo(
                versionName = "1.0.5",
                releaseUrl = "https://github.com/example/releases/v1.0.5",
                apkDownloadUrl = "https://example.com/app.apk",
                apkSize = null,
                changelog = null
            ),
            onDownload = {},
            onDismiss = {}
        )
    }
}
