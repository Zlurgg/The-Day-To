package uk.co.zlurgg.thedayto.feature_mood_color.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MoodColor(
    val mood: String,
    val color: String,
    val dateStamp: Long,
    @PrimaryKey val id: Int? = null
) {
//    companion object {
//        val entryMoodWithColor = listOf(
//            Pair("Happy", RedOrange),
//            Pair("Overjoyed", RedPink),
//            Pair("Sad", BabyBlue),
//            Pair("Angry", Violet),
//            Pair("Depressed", LightGreen),
//        )
//        val defaultMoods = listOf("Happy", "Overjoyed", "Sad", "Angry", "Depressed")
//    }
}

class InvalidMoodColorException(message: String) : Exception(message)