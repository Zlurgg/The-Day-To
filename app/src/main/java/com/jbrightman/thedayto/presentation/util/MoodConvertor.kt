package com.jbrightman.thedayto.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.jbrightman.thedayto.feature_mood_color.domain.model.MoodColor
import com.jbrightman.thedayto.feature_mood_color.presentation.AddEditMoodColorViewModel

fun getColorFromMood(
    mood: String,
): Color? {
    var color: Color? = null
    var isDefaultMood = false
    MoodColor.entryMoodWithColor.forEach {
        if (it.first == mood) {
            color = it.second
            isDefaultMood = true
        }
    }
//    if (!isDefaultMood) {
//        mcViewModel.state.value.moodColors.forEach { moodColors ->
//            if (moodColors.mood == mood) {
//                color = getColor(moodColors.color)
//            }
//        }
//    }
    return color
}

private fun getColor(colorString: String): Color {
    return Color(android.graphics.Color.parseColor("#$colorString"))
}