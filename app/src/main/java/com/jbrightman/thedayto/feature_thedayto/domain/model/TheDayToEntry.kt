package com.jbrightman.thedayto.feature_thedayto.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jbrightman.thedayto.ui.theme.*

@Entity
data class TheDayToEntry(
    val mood: String,
    val content: String,
    val dateStamp: Long,
    val color: Int,
    @PrimaryKey val id: Int? = null
) {
    companion object {
        val entryColors = listOf(RedOrange, LightGreen, Violet, RedPink, BabyBlue)
        val defaultMoods = listOf("Happy", "Sad", "Meh", "Depressed", "Heart-Broken", "Angry", "Overjoyed")
    }
}
class InvalidTheDayToEntryException(message: String): Exception(message)