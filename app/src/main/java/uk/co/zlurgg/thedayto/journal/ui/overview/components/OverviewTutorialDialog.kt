package uk.co.zlurgg.thedayto.journal.ui.overview.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.components.BaseInfoDialog
import uk.co.zlurgg.thedayto.core.ui.components.DialogContentSection
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium

/**
 * Simplified tutorial dialog for welcoming users
 *
 * Shows a brief welcome message explaining the app's core concept.
 * Detailed instructions are provided contextually in EditorTutorialDialog.
 *
 * Accessible anytime from the Overview dropdown menu.
 *
 * Pure presenter component - no business logic, just displays
 * welcome content and triggers callback when dismissed.
 *
 * @param onDismiss Callback when user dismisses the dialog
 */
@Composable
fun OverviewTutorialDialog(
    onDismiss: () -> Unit
) {
    BaseInfoDialog(
        title = stringResource(R.string.tutorial_dialog_title),
        buttonText = stringResource(R.string.tutorial_dialog_button),
        onDismiss = onDismiss
    ) {
        Text(
            text = stringResource(R.string.tutorial_dialog_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.tutorial_dialog_quick_start_title),
            content = stringResource(R.string.tutorial_dialog_quick_start_content)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.tutorial_dialog_tips_title),
            content = stringResource(R.string.tutorial_dialog_tips_content)
        )
    }
}
