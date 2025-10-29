package uk.co.zlurgg.thedayto.journal.ui.overview.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.R

/**
 * Dialog prompting user to create today's entry
 *
 * Shown once per day when the user hasn't logged their mood yet.
 * Provides a gentle reminder without being intrusive.
 *
 * @param onDismiss Callback when user dismisses the dialog (clicks "Not Now" or outside)
 * @param onCreateEntry Callback when user chooses to create an entry (clicks "Create Entry")
 */
@Composable
fun CreateEntryReminderDialog(
    onDismiss: () -> Unit,
    onCreateEntry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.entry_reminder_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(R.string.entry_reminder_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onCreateEntry) {
                Text(stringResource(R.string.create_entry))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.not_now))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun CreateEntryReminderDialogPreview() {
    MaterialTheme {
        CreateEntryReminderDialog(
            onDismiss = {},
            onCreateEntry = {}
        )
    }
}
