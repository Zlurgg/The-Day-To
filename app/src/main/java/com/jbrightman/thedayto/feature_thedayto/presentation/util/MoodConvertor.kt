package com.jbrightman.thedayto.feature_thedayto.presentation.util

import androidx.compose.ui.graphics.Color
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
fun getColorFromMood(mood: String): Color? {
    var color: Color? = null
    TheDayToEntry.entryMoodWithColor.forEach {
        if (it.first == mood) {
            color = it.second
        }
    }
    return color
}
