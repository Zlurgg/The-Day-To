package uk.co.zlurgg.thedayto.journal.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mood_color",
    indices = [
        Index(value = ["mood"]),  // Regular index for display queries
        Index(value = ["moodNormalized"], unique = true)  // Enforce case-insensitive uniqueness
    ]
)
data class MoodColorEntity(
    val mood: String,              // Original case for display (e.g., "Happy")
    val moodNormalized: String,    // Lowercase for uniqueness (e.g., "happy")
    val color: String,
    val isDeleted: Boolean = false,
    val dateStamp: Long,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)
