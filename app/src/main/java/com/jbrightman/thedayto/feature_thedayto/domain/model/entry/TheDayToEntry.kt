package com.jbrightman.thedayto.feature_thedayto.domain.model.entry

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jbrightman.thedayto.ui.theme.*

@Entity
data class TheDayToEntry(
    val mood: String,
    val content: String,
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
class InvalidTheDayToEntryException(message: String): Exception(message)