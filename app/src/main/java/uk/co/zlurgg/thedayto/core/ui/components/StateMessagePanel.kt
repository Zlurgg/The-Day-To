package uk.co.zlurgg.thedayto.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable panel for displaying state messages (empty, error, etc.)
 *
 * Provides consistent styling for empty states and error messages
 * across the app. Centers an icon, title, and message vertically.
 *
 * @param icon Icon to display
 * @param title Main title text
 * @param message Descriptive message text
 * @param modifier Optional modifier
 * @param iconTint Color for the icon (defaults to onSurfaceVariant)
 * @param messageColor Color for the message text (defaults to onSurfaceVariant)
 * @param iconSize Size of the icon (defaults to 64.dp)
 * @param iconSpacing Space between icon and title (defaults to 16.dp)
 * @param textSpacing Space between title and message (defaults to 8.dp)
 */
@Composable
fun StateMessagePanel(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    messageColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconSize: Dp = 64.dp,
    iconSpacing: Dp = 16.dp,
    textSpacing: Dp = 8.dp
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = iconTint
        )
        Spacer(Modifier.height(iconSpacing))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(textSpacing))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = messageColor
        )
    }
}
