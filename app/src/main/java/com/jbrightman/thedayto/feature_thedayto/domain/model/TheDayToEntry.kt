package com.jbrightman.thedayto.feature_thedayto.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TheDayToEntry(
    val mood: String,
    val content: String,
    val dateStamp: Long,
    @PrimaryKey val id: Int? = null
)

class InvalidTheDayToEntryException(message: String): Exception(message)