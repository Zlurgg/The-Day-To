package uk.co.zlurgg.thedayto.journal.ui.editor.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.components.BaseInfoDialog
import uk.co.zlurgg.thedayto.core.ui.components.DialogContentSection
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme

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
    BaseInfoDialog(
        title = stringResource(R.string.editor_tutorial_dialog_title),
        buttonText = stringResource(R.string.editor_tutorial_dialog_button),
        onDismiss = onDismiss
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
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditorTutorialDialogPreview() {
    TheDayToTheme {
        EditorTutorialDialog(
            onDismiss = {}
        )
    }
}
