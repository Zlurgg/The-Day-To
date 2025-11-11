package uk.co.zlurgg.thedayto.journal.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entry",
    foreignKeys = [
        ForeignKey(
            entity = MoodColorEntity::class,
            parentColumns = ["id"],
            childColumns = ["moodColorId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index(value = ["moodColorId"])]
)
data class EntryEntity(
    val moodColorId: Int,
    val content: String,
    val dateStamp: Long,
    @PrimaryKey val id: Int? = null
)
