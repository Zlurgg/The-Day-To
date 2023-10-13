package com.jbrightman.thedayto.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.jbrightman.thedayto.feature_mood_color.domain.model.MoodColor
import com.jbrightman.thedayto.feature_mood_color.presentation.AddEditMoodColorViewModel

/** this should be done correctly, start in the database using mood to get id and then get color from that
 * use use-cases and state vm etc correctly and then can call it via the state rather than manually
 */
fun getColorFromMood(
    mood: String,
): Color {
    var color: Color = Color.White
    var isDefaultMood = false
    MoodColor.entryMoodWithColor.forEach {
        if (it.first == mood) {
            color = it.second
            isDefaultMood = true
        }
    }

    return color
}

