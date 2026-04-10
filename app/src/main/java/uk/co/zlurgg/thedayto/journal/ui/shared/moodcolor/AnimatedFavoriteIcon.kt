package uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.FAVORITE_ANIMATION_DURATION_MS
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.FAVORITE_ICON_SIZE
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.FAVORITE_TOUCH_TARGET
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.ICON_SCALE_FAVORITE
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.ICON_SCALE_UNFAVORITE

@Composable
fun AnimatedFavoriteIcon(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    moodName: String? = null,
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) ICON_SCALE_FAVORITE else ICON_SCALE_UNFAVORITE,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            // Medium stiffness settles faster than Low so rapid taps don't
            // restart a still-settling spring and produce visible jitter.
            stiffness = Spring.StiffnessMedium,
        ),
        label = "favorite_scale",
    )

    val tint by animateColorAsState(
        targetValue = if (isFavorite) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = FAVORITE_ANIMATION_DURATION_MS),
        label = "favorite_tint",
    )

    // Use detailed content description when mood name is provided
    val contentDescription = if (moodName != null) {
        stringResource(
            if (isFavorite) {
                R.string.remove_from_favorites_desc
            } else {
                R.string.add_to_favorites_desc
            },
            moodName,
        )
    } else {
        stringResource(
            if (isFavorite) {
                R.string.remove_from_favorites
            } else {
                R.string.add_to_favorites
            },
        )
    }

    // Custom touch target with centered icon (no extra IconButton padding)
    Box(
        modifier = modifier
            .size(FAVORITE_TOUCH_TARGET)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = FAVORITE_TOUCH_TARGET / 2),
                onClick = onClick,
                role = Role.Button,
                onClickLabel = contentDescription,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isFavorite) {
                Icons.Default.Star
            } else {
                Icons.Outlined.StarBorder
            },
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .size(FAVORITE_ICON_SIZE)
                .scale(scale),
        )
    }
}
