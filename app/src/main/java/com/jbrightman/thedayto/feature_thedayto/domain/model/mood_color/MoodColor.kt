package com.jbrightman.thedayto.feature_thedayto.domain.model.mood_color

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jbrightman.thedayto.ui.theme.BabyBlue
import com.jbrightman.thedayto.ui.theme.LightGreen
import com.jbrightman.thedayto.ui.theme.RedOrange
import com.jbrightman.thedayto.ui.theme.RedPink
import com.jbrightman.thedayto.ui.theme.Violet

@Entity
data class MoodColor(
    val mood: String,
    val color: Float,
    val dateStamp: Long,
    @PrimaryKey val id: Int? = null
) {
    companion object {
        val defaultColors = listOf(RedOrange, LightGreen, Violet, RedPink, BabyBlue)
        val defaultMoods = listOf("Happy", "Sad", "Meh", "Depressed", "Heart-Broken", "Angry", "Overjoyed")
    }
}

class InvalidMoodColorException(message: String): Exception(message)