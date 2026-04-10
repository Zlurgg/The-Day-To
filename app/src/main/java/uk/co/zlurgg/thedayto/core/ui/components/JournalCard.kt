package uk.co.zlurgg.thedayto.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A Card styled to match The Day To's "section container" look:
 * surfaceVariant background with a low default elevation.
 *
 * Used for the calendar grid, entries list, editor entry card, and the
 * mood color management cards. Use a plain [Card] for non-section uses
 * (e.g. CreateEntryPromptCard, which is a CTA on primaryContainer).
 *
 * @param modifier Standard modifier slot
 * @param containerColor Background color, defaults to surfaceVariant
 * @param elevation Resting elevation
 * @param pressedElevation Elevation while pressed, defaults to [elevation]
 *   for non-interactive section cards
 * @param content Card body, scoped as a Column
 */
@Composable
fun JournalCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    elevation: Dp = JournalCardDefaults.Elevation,
    pressedElevation: Dp = elevation,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation,
            pressedElevation = pressedElevation,
        ),
        content = content,
    )
}

object JournalCardDefaults {
    /** Resting elevation for a journal section card. */
    val Elevation: Dp = 1.dp
}
