package uk.co.zlurgg.thedayto.core.ui.notifications

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R

/**
 * Dialog shown when notification permission is permanently denied.
 *
 * This happens when the user denies the permission multiple times,
 * causing Android to stop showing the system permission dialog.
 * The user must manually enable the permission in app settings.
 */
@Composable
fun PermissionPermanentlyDeniedDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.permission_denied_title))
        },
        text = {
            Text(text = stringResource(R.string.permission_denied_message))
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(text = stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
