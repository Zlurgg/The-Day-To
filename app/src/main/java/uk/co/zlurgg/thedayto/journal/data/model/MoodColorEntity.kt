package uk.co.zlurgg.thedayto.journal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_color")
data class MoodColorEntity(
    val mood: String,
    val color: String,
    val dateStamp: Long,
    @PrimaryKey val id: Int? = null
)
