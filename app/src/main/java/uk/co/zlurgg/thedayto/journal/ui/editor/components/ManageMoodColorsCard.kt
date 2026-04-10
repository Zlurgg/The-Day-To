package uk.co.zlurgg.thedayto.journal.ui.editor.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.components.JournalCard
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall

private val IconSize = 24.dp

/**
 * A navigation card that opens the Mood Color Management screen.
 *
 * Sits between the Editor top bar and the entry card, showing the palette
 * icon, a "Mood Colors" label, and a forward arrow. Mirrors the visual
 * pattern of the Stats card on Overview so the two navigation cards in
 * the app feel like siblings.
 *
 * @param onClick Navigate to the mood color management screen
 * @param modifier Standard modifier slot
 */
@Composable
fun ManageMoodColorsCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    JournalCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = paddingMedium, vertical = paddingSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(IconSize),
            )
            Spacer(modifier = Modifier.width(paddingMedium))
            Text(
                text = stringResource(R.string.mood_colors_card_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.manage_mood_colors),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(IconSize),
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ManageMoodColorsCardPreview() {
    TheDayToTheme {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(paddingMedium),
        ) {
            ManageMoodColorsCard(onClick = {})
        }
    }
}
