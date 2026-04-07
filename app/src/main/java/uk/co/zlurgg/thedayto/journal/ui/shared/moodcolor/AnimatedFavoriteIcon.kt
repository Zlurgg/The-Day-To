package uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.FAVORITE_ANIMATION_DURATION_MS
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.ICON_SCALE_FAVORITE
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.ICON_SCALE_UNFAVORITE

@Composable
fun AnimatedFavoriteIcon(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) ICON_SCALE_FAVORITE else ICON_SCALE_UNFAVORITE,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "favorite_scale"
    )

    val tint by animateColorAsState(
        targetValue = if (isFavorite) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = FAVORITE_ANIMATION_DURATION_MS),
        label = "favorite_tint"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isFavorite) {
                Icons.Default.Star
            } else {
                Icons.Outlined.StarBorder
            },
            contentDescription = stringResource(
                if (isFavorite) {
                    R.string.remove_from_favorites
                } else {
                    R.string.add_to_favorites
                }
            ),
            tint = tint,
            modifier = Modifier.scale(scale)
        )
    }
}
