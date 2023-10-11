package com.jbrightman.thedayto.feature_mood_color.domain.model

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
    val color: String,
    val dateStamp: Long,
    @PrimaryKey val id: Int? = null
) {
    companion object {
        val entryMoodWithColor = listOf(
            Pair("Happy", RedOrange),
            Pair("Overjoyed", RedPink),
            Pair("Sad", BabyBlue),
            Pair("Angry", Violet),
            Pair("Depressed", LightGreen),
        )
        val defaultMoods = listOf("Happy", "Overjoyed", "Sad", "Angry", "Depressed")
    }
}

class InvalidMoodColorException(message: String): Exception(message)