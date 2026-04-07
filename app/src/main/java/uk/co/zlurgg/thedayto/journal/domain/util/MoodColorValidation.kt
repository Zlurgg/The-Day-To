package uk.co.zlurgg.thedayto.journal.domain.util

/**
 * Validation constants for mood colors.
 */
object MoodColorValidation {
    /** Length of ARGB hex color (with alpha channel) */
    private const val ARGB_HEX_LENGTH = 8

    /** Regex for valid 6 or 8 character hex color (no # prefix). 8-char includes alpha. */
    val HEX_COLOR_REGEX = Regex("^[A-Fa-f0-9]{6}([A-Fa-f0-9]{2})?$")

    /**
     * Normalizes hex color to 6 characters by stripping alpha channel if present.
     * Color picker libraries often return ARGB (8 chars), but we store RGB (6 chars).
     */
    fun normalizeHexColor(hex: String): String {
        return if (hex.length == ARGB_HEX_LENGTH) {
            hex.substring(2) // Strip alpha prefix (first 2 chars)
        } else {
            hex
        }
    }
}
