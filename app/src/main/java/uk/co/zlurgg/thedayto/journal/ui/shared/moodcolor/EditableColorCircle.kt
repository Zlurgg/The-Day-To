package uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.BORDER_ALPHA
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.COLOR_CIRCLE_SIZE_SMALL
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.EDIT_ICON_ALPHA_ON_DARK
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.EDIT_ICON_ALPHA_ON_LIGHT
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.LUMINANCE_THRESHOLD

@Composable
fun EditableColorCircle(
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = COLOR_CIRCLE_SIZE_SMALL,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = BORDER_ALPHA), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = if (color.luminance() > LUMINANCE_THRESHOLD) {
                Color.Black.copy(alpha = EDIT_ICON_ALPHA_ON_LIGHT)
            } else {
                Color.White.copy(alpha = EDIT_ICON_ALPHA_ON_DARK)
            },
            modifier = Modifier.size(size * 0.5f),
        )
    }
}
