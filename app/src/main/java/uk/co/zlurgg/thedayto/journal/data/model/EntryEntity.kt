package uk.co.zlurgg.thedayto.journal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entry")
data class EntryEntity(
    val mood: String,
    val content: String,
    val dateStamp: Long,
    val color: String,
    @PrimaryKey val id: Int? = null
)
