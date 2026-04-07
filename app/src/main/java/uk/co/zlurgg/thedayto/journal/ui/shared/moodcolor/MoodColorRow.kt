package uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.toColorInt
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorWithCount
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.COLOR_CIRCLE_SIZE_LARGE
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.COLOR_CIRCLE_SIZE_SMALL

@Composable
fun MoodColorRow(
    moodColorWithCount: MoodColorWithCount,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    showEntryCount: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val moodColor = moodColorWithCount.moodColor
    val color = remember(moodColor.color) {
        try {
            Color("#${moodColor.color}".toColorInt())
        } catch (_: Exception) {
            Color.Gray
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = paddingMedium, vertical = paddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Animated star toggle (favorite)
        AnimatedFavoriteIcon(
            isFavorite = moodColor.isFavorite,
            onClick = onToggleFavorite
        )

        // Mood name and optional entry count
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = moodColor.mood,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (showEntryCount) {
                Text(
                    text = pluralStringResource(
                        R.plurals.entry_count,
                        moodColorWithCount.entryCount,
                        moodColorWithCount.entryCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Optional trailing content (for variants)
        trailingContent?.invoke()

        Spacer(modifier = Modifier.width(paddingSmall))

        // Color circle with edit icon
        EditableColorCircle(
            color = color,
            onClick = onEdit,
            size = if (showEntryCount) COLOR_CIRCLE_SIZE_LARGE else COLOR_CIRCLE_SIZE_SMALL
        )
    }
}
