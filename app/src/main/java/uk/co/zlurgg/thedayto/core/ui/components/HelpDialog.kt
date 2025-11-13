package uk.co.zlurgg.thedayto.core.ui.components

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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.help_dialog_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = parseFormattedText(stringResource(R.string.help_dialog_content)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.help_dialog_button),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(paddingMedium)
    )
}

/**
 * Parse simple HTML-like formatting in strings
 * Supports <b>text</b> for bold
 */
private fun parseFormattedText(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val boldRegex = Regex("<b>(.*?)</b>")

        boldRegex.findAll(text).forEach { matchResult ->
            // Add text before the bold tag
            if (matchResult.range.first > currentIndex) {
                append(text.substring(currentIndex, matchResult.range.first))
            }

            // Add bold text
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(matchResult.groupValues[1])
            }

            currentIndex = matchResult.range.last + 1
        }

        // Add remaining text after last bold tag
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
