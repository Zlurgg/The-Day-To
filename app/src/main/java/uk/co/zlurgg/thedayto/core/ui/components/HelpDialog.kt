package uk.co.zlurgg.thedayto.core.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium

/**
 * Comprehensive help dialog for app features
 *
 * Shows detailed instructions on all app features.
 * Accessible from the Overview settings menu.
 * Includes sections on creating entries, mood colors, calendar, notifications, and editing.
 *
 * Pure presenter component - no business logic, just displays
 * help content and triggers callback when dismissed.
 *
 * @param onDismiss Callback when user dismisses the dialog
 */
@Composable
fun HelpDialog(
    onDismiss: () -> Unit
) {
    BaseInfoDialog(
        title = stringResource(R.string.help_dialog_title),
        buttonText = stringResource(R.string.help_dialog_button),
        onDismiss = onDismiss,
        scrollable = true
    ) {
        DialogContentSection(
            title = stringResource(R.string.help_dialog_creating_entries_title),
            content = stringResource(R.string.help_dialog_creating_entries_content)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.help_dialog_mood_colors_title),
            content = stringResource(R.string.help_dialog_mood_colors_content)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.help_dialog_calendar_title),
            content = stringResource(R.string.help_dialog_calendar_content)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.help_dialog_notifications_title),
            content = stringResource(R.string.help_dialog_notifications_content)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.help_dialog_editing_title),
            content = stringResource(R.string.help_dialog_editing_content)
        )
    }
}
