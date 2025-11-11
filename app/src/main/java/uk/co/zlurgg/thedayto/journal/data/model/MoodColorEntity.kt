package uk.co.zlurgg.thedayto.journal.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mood_color",
    indices = [Index(value = ["mood"], unique = true)]
)
data class MoodColorEntity(
    val mood: String,
    val color: String,
    val isDeleted: Boolean = false,
    val dateStamp: Long,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
