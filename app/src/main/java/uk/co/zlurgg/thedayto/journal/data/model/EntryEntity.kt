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
    indices = [
        Index(value = ["moodColorId"]),
        Index(value = ["syncId"], unique = true),
        Index(value = ["userId"]),
        Index(value = ["syncStatus"])
    ]
)
data class EntryEntity(
    val moodColorId: Int,
    val content: String,
    val dateStamp: Long,
    @PrimaryKey val id: Int? = null,
    val syncId: String? = null,
    val userId: String? = null,
    val updatedAt: Long? = null,
    val syncStatus: String = "LOCAL_ONLY"
)
