package uk.co.zlurgg.thedayto.update.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo

/**
 * Dialog to inform user about an available app update.
 * Follows Material 3 design guidelines.
 */
@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Update Available",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Version ${updateInfo.versionName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                updateInfo.apkSize?.let { size ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatFileSize(size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                updateInfo.changelog?.let { changelog ->
                    if (changelog.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "What's new:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = changelog,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDownload) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        },
        modifier = modifier
    )
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdateDialogPreview() {
    MaterialTheme {
        UpdateDialog(
            updateInfo = UpdateInfo(
                versionName = "1.0.4",
                releaseUrl = "https://github.com/example/releases/v1.0.4",
                apkDownloadUrl = "https://example.com/app.apk",
                apkSize = 3_500_000,
                changelog = "- Fixed notification scheduling bug\n- Added update checker\n- Performance improvements"
            ),
            onDownload = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdateDialogNoChangelogPreview() {
    MaterialTheme {
        UpdateDialog(
            updateInfo = UpdateInfo(
                versionName = "1.0.4",
                releaseUrl = "https://github.com/example/releases/v1.0.4",
                apkDownloadUrl = "https://example.com/app.apk",
                apkSize = null,
                changelog = null
            ),
            onDownload = {},
            onDismiss = {}
        )
    }
}
