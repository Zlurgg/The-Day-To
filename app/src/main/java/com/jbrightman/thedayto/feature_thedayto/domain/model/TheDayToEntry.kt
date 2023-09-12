package com.jbrightman.thedayto.feature_thedayto.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TheDayToEntry(
    val mood: String,
    val note: String,
    val timeStamp: Long,
    @PrimaryKey val id: Int? = null
)

class InvalidTheDayToException(message: String): Exception(message)