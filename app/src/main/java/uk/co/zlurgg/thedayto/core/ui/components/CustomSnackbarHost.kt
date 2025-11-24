package uk.co.zlurgg.thedayto.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall

/**
 * Custom styled SnackbarHost with enhanced typography and visual hierarchy.
 *
 * Provides a consistent snackbar appearance across the app with:
 * - Larger, more readable text (bodyLarge for message, labelLarge for action)
 * - Better spacing and padding
 * - Material 3 inverse color scheme
 * - Maximum width constraint for tablet layouts
 * - Optional action button with proper styling
 *
 * @param hostState The SnackbarHostState to observe for showing snackbars
 * @param modifier Optional modifier for the snackbar host
 */
@Composable
fun CustomSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(paddingSmall)
    ) { data ->
        Snackbar(
            modifier = Modifier
                .padding(horizontal = paddingSmall)
                .widthIn(max = 600.dp),
            action = {
                data.visuals.actionLabel?.let { actionLabel ->
                    TextButton(
                        onClick = { data.performAction() },
                        modifier = Modifier.padding(horizontal = paddingSmall)
                    ) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.inversePrimary
                        )
                    }
                }
            },
            dismissAction = null,
            actionOnNewLine = false,
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface
        ) {
            Text(
                text = data.visuals.message,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
