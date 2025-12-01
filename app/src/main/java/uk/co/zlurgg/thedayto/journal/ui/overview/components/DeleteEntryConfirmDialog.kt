package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
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
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.ui.util.datestampToFormattedDate

/**
 * Confirmation dialog for deleting an entry.
 * Shows the entry's mood and date to help user confirm the right entry.
 *
 * @param entry The entry to be deleted
 * @param onConfirm Called when user confirms deletion
 * @param onDismiss Called when dialog should be dismissed (Cancel or back)
 */
@Composable
fun DeleteEntryConfirmDialog(
    entry: EntryWithMoodColor,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = datestampToFormattedDate(entry.dateStamp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_entry_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.delete_entry_dialog_message,
                    entry.moodName,
                    formattedDate
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete_confirm_button),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        modifier = modifier
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DeleteEntryConfirmDialogPreview() {
    TheDayToTheme {
        DeleteEntryConfirmDialog(
            entry = EntryWithMoodColor(
                id = 1,
                moodColorId = 1,
                moodName = "Happy",
                moodColor = "4CAF50",
                content = "Had a great day!",
                dateStamp = 1733011200L // Dec 1, 2024
            ),
            onConfirm = {},
            onDismiss = {}
        )
    }
}
