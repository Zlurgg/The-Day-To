package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.ui.overview.util.OverviewPromptConstants
import uk.co.zlurgg.thedayto.journal.ui.overview.util.UiConstants

/**
 * A card that mimics the look of an entry card but prompts the user
 * to create today's entry. Uses primaryContainer as background with
 * contrasting text, a random prompt as the title, and "+" as the date.
 */
@Composable
fun CreateEntryPromptCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val prompt = remember { OverviewPromptConstants.PROMPTS.random() }
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val textColor = MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = UiConstants.ENTRY_CARD_ELEVATION_DEFAULT,
            pressedElevation = UiConstants.ENTRY_CARD_ELEVATION_PRESSED
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiConstants.ENTRY_CARD_PADDING),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prompt,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.weight(1f, fill = false)
            )
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateEntryPromptCardPreview() {
    TheDayToTheme {
        CreateEntryPromptCard(onClick = {})
    }
}
