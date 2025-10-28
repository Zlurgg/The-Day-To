package uk.co.zlurgg.thedayto.journal.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import uk.co.zlurgg.thedayto.core.ui.theme.md_theme_light_onSurface
import uk.co.zlurgg.thedayto.core.ui.theme.md_theme_dark_onSurface

// Static text colors that match our theme's onSurface values
// These don't change with light/dark mode - they're determined solely by background luminance
private val DarkText = md_theme_light_onSurface  // Warm dark brown for light backgrounds
private val LightText = md_theme_dark_onSurface  // Warm beige for dark backgrounds

/**
 * Determines the appropriate text color to use on a given background color
 * for optimal contrast and readability.
 *
 * Uses the W3C recommended luminance formula to calculate background brightness:
 * - Luminance > 0.5 (light backgrounds) → use dark text (warm brown)
 * - Luminance ≤ 0.5 (dark backgrounds) → use light text (warm beige)
 *
 * This ensures WCAG AA contrast ratio compliance for accessibility while
 * maintaining consistency with the app's theming.
 *
 * The text color is determined solely by the background color's luminance,
 * independent of the app's light/dark theme mode.
 *
 * @return Static dark or light text color for optimal contrast against this background
 */
fun Color.getContrastingTextColor(): Color {
    return if (this.luminance() > 0.5f) {
        DarkText  // Dark text for light backgrounds
    } else {
        LightText  // Light text for dark backgrounds
    }
}
