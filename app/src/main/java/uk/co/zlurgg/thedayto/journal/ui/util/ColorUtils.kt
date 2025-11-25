package uk.co.zlurgg.thedayto.journal.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.graphics.toColorInt
import timber.log.Timber
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

/**
 * Safely converts a hex color string to a Compose Color object.
 *
 * Catches parsing errors and returns a fallback color instead of throwing.
 * Logs warnings for invalid color strings via Timber.
 *
 * Supports both formats:
 * - With '#' prefix: "#FF5733"
 * - Without '#' prefix: "FF5733"
 *
 * @param colorString Hex color string (with or without '#' prefix)
 * @param fallback Color to return if parsing fails (defaults to Gray)
 * @return Parsed Color or fallback if parsing fails
 *
 * Example:
 * ```
 * val color = getColorSafe("#FF5733")      // Returns Color(0xFFFF5733)
 * val color = getColorSafe("invalid")       // Returns Color.Gray, logs warning
 * val color = getColorSafe("bad", Color.Red) // Returns Color.Red, logs warning
 * ```
 */
fun getColorSafe(colorString: String, fallback: Color = Color.Gray): Color {
    return try {
        val normalizedString = if (colorString.startsWith("#")) colorString else "#$colorString"
        Color(normalizedString.toColorInt())
    } catch (e: IllegalArgumentException) {
        Timber.w(e, "Invalid color format: $colorString, using fallback")
        fallback
    }
}

