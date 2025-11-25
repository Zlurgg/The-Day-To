package uk.co.zlurgg.thedayto.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.about_dialog_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.about_dialog_button),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(paddingMedium)
    )
}

@Preview(name = "About Dialog - Light", showBackground = true)
@Preview(name = "About Dialog - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AboutDialogPreview() {
    TheDayToTheme {
        AboutDialog(onDismiss = {})
    }
}
