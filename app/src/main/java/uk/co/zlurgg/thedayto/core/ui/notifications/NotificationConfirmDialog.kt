package uk.co.zlurgg.thedayto.core.ui.notifications

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme

/**
 * Simple confirmation dialog shown after notification permission is granted.
 *
 * Informs the user that notifications are scheduled for 9 AM and provides
 * an option to change the time via the settings dialog.
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onChangeTime Callback when user wants to change notification time
 */
@Composable
fun NotificationConfirmDialog(
    onDismiss: () -> Unit,
    onChangeTime: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.notification_confirm_title))
        },
        text = {
            Text(text = stringResource(R.string.notification_confirm_message))
        },
        confirmButton = {
            TextButton(onClick = onChangeTime) {
                Text(text = stringResource(R.string.change_time))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.ok))
            }
        }
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationConfirmDialogPreview() {
    TheDayToTheme {
        NotificationConfirmDialog(
            onDismiss = {},
            onChangeTime = {}
        )
    }
}
