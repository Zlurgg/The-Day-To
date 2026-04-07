package uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Constants for mood color UI components.
 */
object MoodColorConstants {
    // Animation timing
    const val REORDER_DELAY_MS = 200L
    const val FAVORITE_ANIMATION_DURATION_MS = 200

    // Color circle sizes
    val COLOR_CIRCLE_SIZE_SMALL: Dp = 32.dp
    val COLOR_CIRCLE_SIZE_LARGE: Dp = 44.dp

    // Favorite icon animation
    const val ICON_SCALE_FAVORITE = 1f
    const val ICON_SCALE_UNFAVORITE = 0.8f

    // Edit icon contrast (for light/dark backgrounds)
    const val EDIT_ICON_ALPHA_ON_LIGHT = 0.6f
    const val EDIT_ICON_ALPHA_ON_DARK = 0.8f
    const val LUMINANCE_THRESHOLD = 0.5f

    // Border alpha
    const val BORDER_ALPHA = 0.3f

    // Icon button size (for dropdown items)
    val ICON_BUTTON_SIZE: Dp = 40.dp

    // Card styling (matches entry cards)
    val CARD_ELEVATION_DEFAULT: Dp = 2.dp
    val CARD_ELEVATION_PRESSED: Dp = 4.dp
}
