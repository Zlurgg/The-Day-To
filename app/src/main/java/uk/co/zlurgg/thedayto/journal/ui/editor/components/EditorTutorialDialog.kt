package uk.co.zlurgg.thedayto.journal.ui.editor.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.components.DialogContentSection
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium

/**
 * Editor tutorial dialog for first-time entry creation
 *
 * Shown automatically when user creates their first entry.
 * Explains mood colors, notes, and how to save entries.
 *
 * Pure presenter component - no business logic, just displays
 * instructional content and triggers callback when dismissed.
 *
 * @param onDismiss Callback when user dismisses the dialog
 */
@Composable
fun EditorTutorialDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.editor_tutorial_dialog_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                DialogContentSection(
                    title = stringResource(R.string.editor_tutorial_mood_colors_title),
                    content = stringResource(R.string.editor_tutorial_mood_colors_content)
                )

                Spacer(modifier = Modifier.height(paddingMedium))

                DialogContentSection(
                    title = stringResource(R.string.editor_tutorial_notes_title),
                    content = stringResource(R.string.editor_tutorial_notes_content)
                )

                Spacer(modifier = Modifier.height(paddingMedium))

                DialogContentSection(
                    title = stringResource(R.string.editor_tutorial_saving_title),
                    content = stringResource(R.string.editor_tutorial_saving_content)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.editor_tutorial_dialog_button),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(paddingMedium)
    )
}
