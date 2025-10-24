package uk.co.zlurgg.thedayto.journal.domain.util

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

/** this should be done correctly, start in the database using mood to get id and then get color from that
 * use use-cases and state vm etc correctly and then can call it via the state rather than manually
 */
/*fun getColorFromMoodForDefault(
    mood: String,
): Color {
    var color: Color = Color.White
    MoodColor.entryMoodWithColor.forEach {
        if (it.first == mood) {
            color = it.second
        }
    }
    return color
}*/
fun getColor(colorString: String): Color {
    return Color("#$colorString".toColorInt())
}


