package uk.co.zlurgg.thedayto.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.BuildConfig
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium

/**
 * About dialog with app information
 *
 * Displays detailed information about the app including version,
 * description, privacy policy, open source details, credits, and license.
 *
 * Accessible from settings menu for users who want more information
 * about the application.
 *
 * Pure presenter component - no business logic, just displays
 * information and triggers callback when dismissed.
 *
 * @param onDismiss Callback when user dismisses the dialog
 */
@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    BaseInfoDialog(
        title = stringResource(R.string.about_dialog_title),
        buttonText = stringResource(R.string.about_dialog_button),
        onDismiss = onDismiss,
        scrollable = true
    ) {
        DialogContentSection(
            title = stringResource(R.string.about_dialog_version_title),
            content = stringResource(R.string.about_dialog_version_info, BuildConfig.VERSION_NAME)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.about_dialog_description_title),
            content = stringResource(R.string.about_dialog_description_content)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.about_dialog_privacy_title),
            content = stringResource(R.string.about_dialog_privacy_content)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.about_dialog_open_source_title),
            content = stringResource(R.string.about_dialog_open_source_content)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.about_dialog_credits_title),
            content = stringResource(R.string.about_dialog_credits_content)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.about_dialog_license_title),
            content = stringResource(R.string.about_dialog_license_content)
        )
    }
}

@Preview(name = "About Dialog - Light", showBackground = true)
@Preview(name = "About Dialog - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AboutDialogPreview() {
    TheDayToTheme {
        AboutDialog(onDismiss = {})
    }
}
