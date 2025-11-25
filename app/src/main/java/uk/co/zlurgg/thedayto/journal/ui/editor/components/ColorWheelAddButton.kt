package uk.co.zlurgg.thedayto.journal.ui.editor.components

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme

/**
 * A circular button with a color wheel gradient background and a "+" icon overlay.
 *
 * Used as a visually prominent way to trigger the mood color creation dialog,
 * making it immediately obvious to users that they can create custom mood colors.
 *
 * @param onClick Callback when button is clicked
 * @param modifier Optional modifier
 * @param size Button diameter (default 40.dp for Material touch target)
 */
@Composable
fun ColorWheelAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val rainbowColors = listOf(
        Color(0xFFFF0000), // Red
        Color(0xFFFF7F00), // Orange
        Color(0xFFFFFF00), // Yellow
        Color(0xFF00FF00), // Green
        Color(0xFF0000FF), // Blue
        Color(0xFF4B0082), // Indigo
        Color(0xFF9400D3), // Violet
        Color(0xFFFF0000)  // Back to red to complete the circle
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
                role = Role.Button,
                onClickLabel = stringResource(R.string.add_mood_color_button_description)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Color wheel background
        Canvas(modifier = Modifier.size(size)) {
            // Fill with sweep gradient
            drawCircle(
                brush = Brush.sweepGradient(rainbowColors),
                radius = size.toPx() / 2
            )
            // Optional: slight inner shadow/darker ring for depth
            drawCircle(
                color = Color.Black.copy(alpha = 0.1f),
                radius = size.toPx() / 2 - 2.dp.toPx(),
                style = Stroke(width = 4.dp.toPx())
            )
        }

        // Plus icon with white fill and dark stroke for visibility
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.add_mood_color_button_description),
            tint = Color.White,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Preview(name = "Color Wheel Add Button - Light", showBackground = true)
@Preview(name = "Color Wheel Add Button - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ColorWheelAddButtonPreview() {
    TheDayToTheme {
        ColorWheelAddButton(onClick = {})
    }
}

@Preview(name = "Color Wheel Add Button - Large", showBackground = true)
@Composable
private fun ColorWheelAddButtonLargePreview() {
    TheDayToTheme {
        ColorWheelAddButton(onClick = {}, size = 56.dp)
    }
}
