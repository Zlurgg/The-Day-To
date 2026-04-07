package uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import uk.co.zlurgg.thedayto.core.ui.theme.paddingExtraSmall
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
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorWithCount
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.COLOR_CIRCLE_SIZE_LARGE
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.COLOR_CIRCLE_SIZE_SMALL

/**
 * Unified row component for displaying a mood color with favorite toggle and edit.
 *
 * @param moodColor The mood color to display
 * @param onToggleFavorite Called when user toggles the favorite star
 * @param onEdit Called when user taps the color circle to edit
 * @param modifier Modifier for the row
 * @param entryCount Optional entry count to display (null = hidden, used in Management)
 * @param compact True for dropdown sizing (smaller circle), false for full list view
 * @param trailingContent Optional content between mood name and color circle
 */
@Composable
fun MoodColorRow(
    moodColor: MoodColor,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    entryCount: Int? = null,
    compact: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val color = remember(moodColor.color) {
        try {
            Color("#${moodColor.color}".toColorInt())
        } catch (_: Exception) {
            Color.Gray
        }
    }

    // Use larger circle when showing entry count (Management), smaller for compact (dropdown)
    val circleSize = when {
        compact -> COLOR_CIRCLE_SIZE_SMALL
        entryCount != null -> COLOR_CIRCLE_SIZE_LARGE
        else -> COLOR_CIRCLE_SIZE_SMALL
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = paddingExtraSmall,
                end = paddingMedium,
                top = paddingSmall,
                bottom = paddingSmall
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Animated star toggle (favorite) with accessibility
        AnimatedFavoriteIcon(
            isFavorite = moodColor.isFavorite,
            onClick = onToggleFavorite,
            moodName = moodColor.mood
        )

        Spacer(modifier = Modifier.width(paddingExtraSmall))

        // Mood name and optional entry count
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = moodColor.mood,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (entryCount != null) {
                Text(
                    text = pluralStringResource(
                        R.plurals.entry_count,
                        entryCount,
                        entryCount
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
            size = circleSize
        )
    }
}

/**
 * Convenience overload for MoodColorManagement screen using MoodColorWithCount.
 */
@Composable
fun MoodColorRow(
    moodColorWithCount: MoodColorWithCount,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    showEntryCount: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null
) {
    MoodColorRow(
        moodColor = moodColorWithCount.moodColor,
        onToggleFavorite = onToggleFavorite,
        onEdit = onEdit,
        modifier = modifier,
        entryCount = if (showEntryCount) moodColorWithCount.entryCount else null,
        compact = false,
        trailingContent = trailingContent
    )
}
