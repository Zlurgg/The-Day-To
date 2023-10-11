package com.jbrightman.thedayto.presentation.util

import androidx.compose.ui.graphics.Color
import com.jbrightman.thedayto.feature_daily_entry.domain.model.DailyEntry
fun getColorFromMood(mood: String): Color? {
    var color: Color? = null
    DailyEntry.entryMoodWithColor.forEach {
        if (it.first == mood) {
            color = it.second
        }
    }
    return color
}
