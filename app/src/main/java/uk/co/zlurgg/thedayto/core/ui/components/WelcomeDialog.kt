package uk.co.zlurgg.thedayto.core.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium

/**
 * Welcome dialog for first-time users
 *
 * Displayed before sign-in to provide context about the app,
 * its features, and privacy information. Helps users understand
 * what they're signing into before authentication.
 *
 * Pure presenter component - no business logic, just displays
 * information and triggers callback when dismissed.
 *
 * @param onDismiss Callback when user dismisses the dialog
 */
@Composable
fun WelcomeDialog(
    onDismiss: () -> Unit
) {
    BaseInfoDialog(
        title = stringResource(R.string.welcome_dialog_title),
        buttonText = stringResource(R.string.welcome_dialog_button),
        onDismiss = onDismiss
    ) {
        Text(
            text = stringResource(R.string.welcome_dialog_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.welcome_dialog_features_title),
            content = stringResource(R.string.welcome_dialog_features_list)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        DialogContentSection(
            title = stringResource(R.string.welcome_dialog_privacy_title),
            content = stringResource(R.string.welcome_dialog_privacy_info)
        )
    }
}
