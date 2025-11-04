package uk.co.zlurgg.thedayto.core.ui.notifications

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R

/**
 * Warning dialog shown when user tries to enable notifications but they're
 * disabled at the system level (Android Settings > Apps > [App] > Notifications).
 *
 * This helps users understand why notifications aren't working and guides them
 * to the correct settings screen to fix the issue.
 *
 * User Flow:
 * 1. User tries to enable notifications in app
 * 2. System check fails (notifications disabled in Android Settings)
 * 3. This dialog appears with explanation
 * 4. User clicks "Open Settings" → Taken to Android Settings
 * 5. User enables notifications in system settings
 * 6. User returns to app and tries again → Works!
 *
 * @param onDismiss Callback when dialog is dismissed without action
 * @param onOpenSettings Callback when user chooses to open system settings
 */
@Composable
fun SystemNotificationDisabledDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.notifications_disabled_title))
        },
        text = {
            Text(text = stringResource(R.string.notifications_disabled_message))
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
