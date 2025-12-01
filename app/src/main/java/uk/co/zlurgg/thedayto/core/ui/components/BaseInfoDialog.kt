package uk.co.zlurgg.thedayto.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme

/**
 * Reusable base dialog for informational content
 *
 * Provides consistent styling for all info dialogs (Help, About, Tutorial, Welcome).
 * Encapsulates common AlertDialog setup: title styling, container color, padding,
 * and dismiss button.
 *
 * @param title Dialog title displayed at the top
 * @param buttonText Text for the dismiss button
 * @param onDismiss Callback when dialog is dismissed
 * @param modifier Optional modifier for the dialog
 * @param scrollable Whether the content should be scrollable (default: false)
 * @param content Composable content displayed in the dialog body
 */
@Composable
fun BaseInfoDialog(
    title: String,
    buttonText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (scrollable) Modifier.verticalScroll(rememberScrollState())
                        else Modifier
                    ),
                content = content
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = buttonText,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.padding(paddingMedium)
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BaseInfoDialogPreview() {
    TheDayToTheme {
        BaseInfoDialog(
            title = "Sample Dialog",
            buttonText = "Got It",
            onDismiss = {}
        ) {
            Text(
                text = "This is sample content for the dialog preview.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
