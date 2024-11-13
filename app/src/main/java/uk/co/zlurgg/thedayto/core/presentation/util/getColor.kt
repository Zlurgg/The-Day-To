package uk.co.zlurgg.thedayto.core.presentation.util

import androidx.compose.ui.graphics.Color

/** this should be done correctly, start in the database using mood to get id and then get color from that
 * use use-cases and state vm etc correctly and then can call it via the state rather than manually
 */

fun getColor(colorString: String): Color {
    return Color(android.graphics.Color.parseColor("#$colorString"))
}


