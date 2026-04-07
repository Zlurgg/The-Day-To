package uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.components

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
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor

/**
 * Confirmation dialog for deleting a mood color.
 * Shows the mood name to help user confirm the right item.
 *
 * @param moodColor The mood color to be deleted
 * @param onConfirm Called when user confirms deletion
 * @param onDismiss Called when dialog should be dismissed (Cancel or back)
 */
@Composable
fun DeleteMoodColorConfirmDialog(
    moodColor: MoodColor,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_mood_color_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.delete_mood_color_dialog_message,
                    moodColor.mood
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
private fun DeleteMoodColorConfirmDialogPreview() {
    TheDayToTheme {
        DeleteMoodColorConfirmDialog(
            moodColor = MoodColor(
                id = 1,
                mood = "Happy",
                color = "4CAF50",
                dateStamp = System.currentTimeMillis()
            ),
            onConfirm = {},
            onDismiss = {}
        )
    }
}
