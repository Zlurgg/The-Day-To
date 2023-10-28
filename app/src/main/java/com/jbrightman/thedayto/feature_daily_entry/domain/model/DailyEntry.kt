package com.jbrightman.thedayto.feature_daily_entry.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DailyEntry(
    val mood: String,
    val content: String,
    val dateStamp: Long,
    val color: String,
    @PrimaryKey val id: Int? = null
)
class InvalidDailyEntryException(message: String): Exception(message)